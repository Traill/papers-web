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

  import scala.util.Random.nextDouble

  // Shorthand for the rows and columns of the matrix
  val n : Int = T.rows
  val k : Int = T.cols
  val is : Range = 0 to (n - 1)
  val js : Range = 0 to (k - 1)

  // The initialized grouping (I could also import random and use 'shuffle')
  val inits : Map[Int, Seq[Int]] = is.groupBy(i => i % k)
  //val inits : Map[Int, Seq[Int]] = is.groupBy(_ => (nextDouble * k).toInt)


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


case class Community(docs : Map[String, Document])

case class Louvain(docs : Map[String, Document]) {

  // For use maybe later
  import scala.util.Random.nextInt

  // Initial clustering: Each document is its own cluster
  lazy val init : Map[String, Community] = for ((id, d) <- docs) yield (id -> Community(Map(id -> d)))

  // Total value of edges in graph
  lazy val m = (for ((_,d) <- docs; l <- d.links) yield l.weight).sum

  // Map of the total value of edges per ID
  lazy val K : Map[String, Int] = for ((id, d) <- docs) yield (id -> d.links.map(_.weight).sum)

  // A stable index assignment so each paper has an index between 0 and (a.docs.length -1)
  lazy val indexToId : Map[Int, String] = for (((id, _), index) <- docs.zipWithIndex) yield (index -> id)

  // Returns a random document. This might not be needed
  def randDoc : Document = docs(indexToId(nextInt(docs.size)))

  // Does the link lead to the same community
  def sameAs (c : Community, cs : Map[String,Community])(l : Link) = cs(l.id) == c

  // The community score
  def Q(cs : Map[String, Community]) : Double = {

    // Utility functions for calculating Q
    val getWeight = (id : String) => (l : Link) => l.weight - K(id) * K(l.id) / (2.0 * m)

    // Variables along the way
    val norm = 1.0/(2.0 * m)
    val weights = for ((id, c) <- cs; (id, d) <- c.docs) yield {
                     d.links.filter(sameAs(c,cs)).map(getWeight(id)).sum
                  }

    // Return the sum of the normalized weights
    return norm * (weights).sum
  }

  // Returns the difference in Q by adding document 'd' to community 'c'
  def deltaQ(cs : Map[String, Community], c : Community, d : Document) : Double = {

    // Sum of inner links in a community
    val Sin = (for ((id, d) <- c.docs; l <- d.links if sameAs(c,cs)(l)) yield l.weight).sum

    // Sum of all links in a community
    val Stot = (for ((id, d) <- c.docs; l <- d.links) yield (l.weight)).sum

    // Sum of edges leading from document into community
    val kiin = (for (l <- d.links if sameAs(c,cs)(l)) yield l.weight).sum

    // Return the delta Q
    return ((Sin + 2.0 * kiin) / (2.0 * m) 
                - math.pow((Stot + K(d.id)) / (2.0 * m), 2) 
                - (Sin/(2.0 * m) 
                  - math.pow(Stot/(2.0*m), 2) 
                  - math.pow(K(d.id)/(2.0*m), 2)))
  }

  // Moves a document 'd' to cluster 'c'
  def move(cs : Map[String, Community], c : Community, d : Document) : Map[String, Community] = { 

    // Remove d from its former community and add it to it's new one
    val r = Community(cs(d.id).docs - d.id)
    val a = Community(c.docs + (d.id -> d))

    // Make the id's of the other elements of each community point to the updated version
    val moveNew = (for ((id, _) <- c.docs) yield (id -> a))
    val moveOld = (for ((id, _) <- r.docs) yield (id -> r))

    return cs + (d.id -> a) ++ moveNew ++ moveOld
  }

  // Do one iteration
  def iter(cs : Map[String, Community])(n : Int) : Map[String, Community] = { 

    // Get a few things straight
    val id = indexToId(n % docs.size)
    val d = docs(id)
    val c = cs(id)

    // Remove a document linked to by 'l' from the community where it is at
    def remove(l : Link) : Community = Community(cs(l.id).docs - l.id)

    // The gain achieved by adding the document linked by 'l' to 'c'
    def posQ(l : Link) : Double = deltaQ(cs, c, docs(l.id))
    // The loss achieved by removing the document linked by 'l' from its community
    def negQ(l : Link) : Double = -1.0 * deltaQ(cs, remove(l), docs(l.id))

    // list of the changes in Q for each link in d
    val qs : Map[String, Double] = (for (l <- d.links if (!sameAs(c,cs)(l))) yield {
                                      (l.id -> (posQ(l) + negQ(l)))
                                   }).toMap

    // The id which corresponds to the maximum change
    val (maxId, maxVal) = if (qs == Map.empty) ("0",-1.0) else qs.maxBy(_._2)

    // Move the document with the highest gain to the community if it's over 0
    return if (maxVal > 0) move(cs, c, docs(maxId)) else cs
  }

  // Do an iteration over every node
  def cycle(init : Map[String, Community]) : Map[String, Community] = {
    return (0 to (docs.size - 1)).foldLeft(init)((a,b) => iter(a)(b))
  }

  // A stream where each element is the clustering after another cycle of iterations
  val cycleStream : Stream[Map[String, Community]] = init #:: cycleStream.map(cycle(_))

  
  def cluster : Map[String, Int] = {

    // The result is the first element where there are no changes in between two cycles
    lazy val result = cycleStream.zip(cycleStream.tail).takeWhile({ case (m1,m2) => m1 != m2 }).toList.last._2

    // A list of communities with their indices
    lazy val list = result.values.toList.distinct.zipWithIndex
    
    // A list of id's each associated with a group
    lazy val grouping = (for ((c, i) <- list; (id, _) <- c.docs) yield (id -> i)).toMap

    return grouping
  }


  def print(cs : Map[String, Community]) : Unit = {
    val csList = cs.values.toList.distinct
    for ((Community(ds), i) <- csList.zipWithIndex) { println(i + ": \t" + ds.keys.mkString(", ")); }
  }
}
