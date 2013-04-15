package paper

trait Bibliographic extends CompareData {

  def getWeight(d1 : DataItem, d2 : DataItem, i1 : Int, i2 : Int) : Int = {
    // Get names
    val p1Names = getDistinctNames(d1)
    val p2Names = getDistinctNames(d2)

    // For each auther in p, check if he/she exists in other
    var matches = (for (name <- p1Names if p2Names.contains(name)) yield 1).sum

    // return result
    return (100 * matches.toDouble / p1Names.length.toDouble).toInt
  }

  // Get a list of all the distinct names in the paper authors and references
  def getDistinctNames(d : DataItem) : List[String] = d match {
    case Paper(_,authors,_,_,refs) => {
      val as = authors ::: refs.flatMap(r => r.authors)
      as.map(a => a.name).distinct
    }
    case Course(_,profs,_) => {
      val as = profs.map(r => r.name)
      as.distinct
    }
    case JustText(_) => List()
  }
}
