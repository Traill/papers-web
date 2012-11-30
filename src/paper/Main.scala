package paper


object Main {
  def main(args : Array[String]): Unit= {
    // create analyzer
    val A : Analyzer = Analyzer(Map.empty)

    var t = A.initialize("resources/isit2012test").load.link

    lazy val c = Cluster(t)

  }

  // So far this is just whatever code I happen to be testing
  case class Cluster(a : Analyzer) {

    // Get some linear algebra on the line
    import breeze.linalg._

    lazy val n : Int = links.size
    lazy val k : Int = 2 // Let's test with a graph partition for now

    // Map from id's to indices
    lazy val idToIndex : Map[String,Int] = for (((id, _), index) <- a.docs.zipWithIndex) yield (id -> index)

    lazy val linksToIndex : List[Link] => Map[Int,Int] = (ls : List[Link]) => (for (Link(id, weight) <- ls) yield (idToIndex(id) -> weight)).toMap

    // Convenient format for Links
    lazy val links : Map[Int,Map[Int,Int]] = (for (Document(id, _, _, ls, _) <- a.docs.values) yield (idToIndex(id) -> linksToIndex(ls))).toMap

    // Adjecency Matrix
    lazy val W : DenseMatrix[Double] = DenseMatrix.eye[Double](n).mapPairs({ case ((i, j),t) => if (links(i).contains(j)) (links(i)(j)).toFloat/100 else 0.0 } )

    // Degree Matrix
    lazy val D : DenseMatrix[Double] = diag((W * DenseVector.ones[Double](links.size)))

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

  // Reads in the options and converts them to a map of options
  def readOptions(s : String) : Map[String,Boolean] = {

    var options : Map[String,Boolean] = Map();

    if (s.length > 0 && s(0) == '-') {
      // Define options as always false, except
      options = Map().withDefaultValue(false)

      // Check for parsing
      if (s.contains('p')) options += ("parse" -> true)

      // Check for getting schedule
      if (s.contains('s')) options += ("xmlschedule" -> true)

      // Check for extending
      if (s.contains('e')) options += ("extend" -> true)

      // Check for linking
      if (s.contains('l')) options += ("link" -> true)

      // Check for linking
      if (s.contains('g')) options += ("graph" -> true)

      if(s.contains('h')) { 
        println("""How to call: Analyze [path] 
        [parameter]?\nPARAMETERS:\n\t-p : parsing\n\t-s : looks for xml 
        scheduler\n\t-c : compare\n\t-l : link\n\t-g : create graph\n\t-h 
        : shows this help page\n\tnothing : do everything"""); 
      }

    }

    else {
      // If no options are supplied we do everything by default
      options = Map().withDefaultValue(true)
    }

    return options
  }
}
