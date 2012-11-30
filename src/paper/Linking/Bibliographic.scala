package paper

trait Bibliographic extends ComparePaper {

  def getWeight(p : Paper, o : Paper, i1 : Int, i2 : Int) : Int = {
    // Get names
    val pNames = Document.getDistinctNames(p)
    val oNames = Document.getDistinctNames(o)

    // For each auther in p, check if he/she exists in other
    var matches = (for (name <- pNames if oNames.contains(name)) yield 1).sum

    // return result
    return (100 * matches.toDouble / pNames.length.toDouble).toInt

  }
}
