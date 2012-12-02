package paper

import breeze.linalg._

abstract class Cluster {

  // The analyzer given as an argument in implementing classes
  val a : Analyzer

  lazy val n : Int = links.size

  // Map from id's to indices
  lazy val idToIndex : Map[String,Int] = for (((id, _), index) <- a.docs.zipWithIndex) yield (id -> index)

  lazy val linksToIndex : List[Link] => Map[Int,Int] = (ls : List[Link]) => (for (Link(id, weight) <- ls) yield (idToIndex(id) -> weight)).toMap

  // Convenient format for Links
  lazy val links : Map[Int,Map[Int,Int]] = (for (Document(id, _, _, ls, _) <- a.docs.values) yield (idToIndex(id) -> linksToIndex(ls))).toMap

  // Adjecency Matrix
  lazy val W : DenseMatrix[Double] = DenseMatrix.eye[Double](n).mapPairs({ case ((i, j),t) => if (links(i).contains(j)) (links(i)(j)).toFloat/100 else 0.0 } )

  // Degree Matrix
  lazy val D : DenseMatrix[Double] = diag((W * DenseVector.ones[Double](links.size)))

}



// Code for spectral clustering
case class Spectral(a : Analyzer) extends Cluster {

  lazy val k : Int = 2 // Let's test with a graph partition for now

  // Inverse square root of degree matrix
  lazy val sqrtInvD : DenseMatrix[Double] = diag(diag(D).map(t => 1.0/scala.math.sqrt(t)))

  // Laplacian
  lazy val L : DenseMatrix[Double] = DenseMatrix.eye[Double](links.size) - sqrtInvD * W * sqrtInvD

  // Because of numerical inaccuracies I need to make my matrix explicitly symmetric
  lazy val Lsym : DenseMatrix[Double] = lowerTriangular(L).t + lowerTriangular(L) - DenseMatrix.eye[Double](n)

  // Compute the eigenvalues and vectors of the Lsym matrix
  lazy val (eigVal, Some(eigVec)) = eigSym(Lsym, true)

  // Get Matrix of the amount of eigenvectors needed
  lazy val U : DenseMatrix[Double] = eigVec(0 to (n - 1), 0 to (k - 1))

  // Get a vector with all the normalization values of U
  lazy val Unorm : DenseVector[Double] = DenseVector.zeros[Double](U.rows).mapPairs( { case (k,v) => math.sqrt(U(k,0 to (U.cols - 1)).map(x => x*x).sum) })

  // Normalize each row of U
  lazy val T : DenseMatrix[Double] = U.mapPairs({ case((i,j),k) => k/Unorm(j) })

}


case class KMeans(T : DenseMatrix[Double]) {

  import scala.util.Random.nextBoolean

  // Shorthand for the rows and columns of the matrix
  val n : Int = T.rows
  val k : Int = T.cols
  val is : Range = 0 to (n - 1)
  val js : Range = 0 to (k - 1)

  // The initialized grouping (I could also import random and use 'shuffle')
  //val inits : Map[Int, Seq[Int]] = is.groupBy(i => i % 2)
  val inits : Map[Int, Seq[Int]] = is.groupBy(_ => if (nextBoolean) 0 else 1)


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
    val distanceList = for (j <- js) yield (j, distance(row, means(j)))

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


  // Find the convergent grouping
  lazy val result = groupStream.zip(groupStream.tail).takeWhile({ case (m1,m2) => m1 != m2 }).toList.last._2

  // fips = 0 : 1 : fips zip $ (fips tail) map $ \(i1,i2) -> i1 + i2 

}
