package paper


object Main {
  def main(args : Array[String]): Unit= {
    // create analyzer
    val A : Analyzer = Analyzer(Map.empty)

    var t = A.initialize("resources/isit2012test").load.link

    val c = Cluster(t)

  }

  // So far this is just whatever code I happen to be testing
  case class Cluster(a : Analyzer) {

    // Get some linear algebra on the line
    import breeze.linalg._

    lazy val n : Int = links.size

    // Map from id's to indices
    lazy val idToIndex : Map[String,Int] = for (((id, _), index) <- a.docs.zipWithIndex) yield (id -> index)

    // Convenient format for Links
    lazy val links : Map[Int,Map[Int,Int]] = (for (Document(id, _, _, ls, _) <- a.docs.values) yield (idToIndex(id) -> linksToIndex(ls))).toMap

    // Adjecency Matrix
    lazy val W : DenseMatrix[Double] = DenseMatrix.eye[Double](n).mapPairs({ case ((i, j),t) => if (links(i).contains(j)) (links(i)(j)).toFloat/100 else 0.0 } )

    // Degree Matrix
    lazy val D : DenseMatrix[Double] = diag((adjecencyMat * DenseVector.ones[Double](links.size)))

    // Inverse square root of degree matrix
    lazy val sqrtInvD : DenseMatrix[Double] = inv(D.map(t => scala.math.sqrt(t)))

    // Laplacian
    lazy val L : DenseMatrix[Double] = DenseMatrix.eye[Double](links.size) - sqrtInvD * W * sqrtInvD

    // Because of numerical inaccuracies I need to make my matrix explicitly symmetric
    lazy val Lsym : DenseMatrix[Double] = lowerTriangular(L).t + lowerTriangular(L) - DenseMatrix.eye[Double](n)

    // Compute the eigenvalues and vectors of the Lsym matrix
    val (eigVal, Some(eigVec)) = eigSym(Lsym, true)

   

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
