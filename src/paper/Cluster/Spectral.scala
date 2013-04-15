package paper

import breeze.linalg._

/**
 * Abstract class implementing some basic things to get spectral clustering running
 */
abstract class Clustering[A <: DataItem] {

  // The analyzer given as an argument in implementing classes
  val docs : Map[String, Document[A]]

  // Number of edges
  lazy val n : Int = links.size

  // Convenient format for Links
  lazy val links : Map[Int,Map[Int,Int]] = for ((id, d) <- docs) yield (idToIndex(id) -> linksToIndex(d.links))

  // Adjecency Matrix
  lazy val W : DenseMatrix[Double] = DenseMatrix.eye[Double](n).mapPairs({ case ((i, j),t) => if (links(i).contains(j)) (links(i)(j)).toFloat/100 else 0.0 } )

  // Degree Matrix
  lazy val D : DenseMatrix[Double] = diag((W * DenseVector.ones[Double](links.size)))

  // Map from id's to indices
  lazy val idToIndex : Map[String,Int] = for (((id, _), index) <- docs.zipWithIndex) yield (id -> index)
  lazy val indexToId : Map[Int,String] = for (((id, _), index) <- docs.zipWithIndex) yield (index -> id)

  // Function transforming a list of links to a map of link indicies
  lazy val linksToIndex : List[Link] => Map[Int,Int] = (ls : List[Link]) => (for (Link(id, weight) <- ls) yield (idToIndex(id) -> weight)).toMap

}

// Code for spectral clustering, k is the maximum amount of clusters
case class Spectral[A <: DataItem](docs : Map[String, Document[A]], k : Int) extends Clustering[A] {

  // Inverse square root of degree matrix
  lazy val sqrtInvD : DenseMatrix[Double] = diag(diag(D).map(t => 1.0/scala.math.sqrt(t)))

  // Laplacian
  lazy val L : DenseMatrix[Double] = DenseMatrix.eye[Double](links.size) - sqrtInvD * W * sqrtInvD

  // Because of numerical inaccuracies I need to make my matrix explicitly symmetric
  lazy val Lsym : DenseMatrix[Double] = lowerTriangular(L).t + lowerTriangular(L) - DenseMatrix.eye[Double](n)

  // Compute the eigenvalues and vectors of the Lsym matrix
  lazy val (eigVal, Some(eigVec)) = eigSym(Lsym, true)

  // To avoid calculating the eigen vectors multiple times for different
  // cluster sizes, I've included a parameter 'm' to decide the cluster size

  // Get Matrix of the amount of eigenvectors needed
  def U(size : Int) : DenseMatrix[Double] = eigVec(0 to (n - 1), 0 to (size - 1))

  // Get a vector with all the normalization values of U
  def Unorm(size : Int) : DenseVector[Double] = DenseVector.zeros[Double](U(size).rows).mapPairs( { case (k,v) => math.sqrt(U(size)(k,0 to (U(size).cols - 1)).map(x => x*x).sum) })

  // Normalize each row of U
  def T(size : Int) : DenseMatrix[Double] = U(size).mapPairs({ case((i,j),k) => k/Unorm(size)(j) })

  // cluster
  def cluster : Map[Int, Map[String, Int]] = {
  println(W)
    println(Lsym)
    val groups = ((2 to k).zipWithIndex).toMap map { case (size,_) =>
      println("Calculating cluster of size: " + size);

      // Define a map from id's to groups
      val grouping = (for ((group, ids) <- KMeans(T(size)).result; index <- ids) yield {
        (indexToId(index) -> group)
      }).toMap

      // Then return a pair with the size pointing to this grouping
      (size -> grouping)
    }

    // Convert the list of sizes to a map
    return groups.toMap
  }
}


case class KMeans(T : DenseMatrix[Double]) {

  // Shorthand for the rows and columns of the matrix
  val n : Int = T.rows
  val k : Int = T.cols
  val is : Range = 0 to (n - 1)
  val js : Range = 0 to (k - 1)

  // The initialized grouping (I could also import random and use 'shuffle')
  val inits : Map[Int, Seq[Int]] = is.groupBy(i => i % k)


  // calculate the new means giving the cluster assignments
  def getMeans(clusters : Map[Int, Seq[Int]]) : Map[Int,Seq[Double]] = {
    for ((group, rows) <- clusters) yield (group, js.map(j => mean(T(rows, j :: Nil))))
  }


  // The euclidian distance between a given row and a mean
  def distance(row : Int, mean : Seq[Double]) : Double = {
    (for (((i,j), v) <- T(row, js).iterator) yield math.pow(mean(j) - v, 2)).sum
  }


  // Assign a group to a row by finding distances to every group
  def assign(row : Int, means : Map[Int, Seq[Double]]) : Int = {

    // Calculate a list of differences
    val distanceList = for (j <- js if means.contains(j)) yield (j, distance(row, means(j)))

    // Find the minimum distance and return the index
    distanceList.reduce((a,b) => if (a._2 < b._2) a else b)._1
  }


  // Returns the groupins given the means of each group
  def getGroups(means : Map[Int, Seq[Double]]) : Map[Int, Seq[Int]] = {
    is.groupBy(assign(_, means))
  }


  // Lazy stream that calculates the groupings. It's corecursive with meanStream
  val groupStream : Stream[Map[Int, Seq[Int]]] = {
    inits #:: meanStream.map(means => getGroups(means))
  }

  // Lazy stream that calculates the means. It's corecursive with groupStream
  val meanStream : Stream[Map[Int, Seq[Double]]] = {
    groupStream.map(grouping => getMeans(grouping))
  }

  // Find the convergent grouping (this is really neat)
  lazy val result : Map[Int, Seq[Int]] = {
    //groupStream.zipWithIndex.takeWhile { case (g,i) => !groupStream.take(i-1).contains(g) } toList.last._1
    groupStream.zip(groupStream.tail).takeWhile({ case (m1,m2) => m1 != m2 }).toList.last._2
  }

}
