package paper

import breeze.linalg._

abstract class Cluster {

  // The analyzer given as an argument in implementing classes
  val docs : Map[String, Document]

  lazy val n : Int = links.size

  // Map from id's to indices
  lazy val idToIndex : Map[String,Int] = for (((id, _), index) <- docs.zipWithIndex) yield (id -> index)
  lazy val indexToId : Map[Int,String] = for (((id, _), index) <- docs.zipWithIndex) yield (index -> id)

  lazy val linksToIndex : List[Link] => Map[Int,Int] = (ls : List[Link]) => (for (Link(id, weight) <- ls) yield (idToIndex(id) -> weight)).toMap

  // Convenient format for Links
  lazy val links : Map[Int,Map[Int,Int]] = (for (Document(id, _, _, ls, _, _) <- docs.values) yield (idToIndex(id) -> linksToIndex(ls))).toMap

  // Adjecency Matrix
  lazy val W : DenseMatrix[Double] = DenseMatrix.eye[Double](n).mapPairs({ case ((i, j),t) => if (links(i).contains(j)) (links(i)(j)).toFloat/100 else 0.0 } )

  // Degree Matrix
  lazy val D : DenseMatrix[Double] = diag((W * DenseVector.ones[Double](links.size)))

}



// Code for spectral clustering, k is the maximum amount of clusters
case class Spectral(docs : Map[String, Document], k : Int) extends Cluster {

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
  lazy val cluster = for (size <- (2 to k); (group, ids) <- KMeans(T(size)).result; index <- ids) yield (indexToId(index) -> (size -> group))
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
  lazy val result = groupStream.zip(groupStream.tail).takeWhile({ case (m1,m2) => m1 != m2 }).toList.last._2
}
