package paper
import java.io._
import scala.io.Source

abstract trait ComparePaper {

  // If there is anything to be initialized it is done here
  def init(papers : List[Paper]) : Unit = ()

  // Returns a weight between 0 and 100
  def getWeight(p1 : Paper, p2 : Paper, i1 : Int, i2 : Int) : Int

  // Creates a list of links
  def makeLinks(papers : Map[String, Paper]) : Map[String, List[Link]] = {

    // We need an indexed version of the papers
    val (ids, ps) = papers.unzip

    // Before we make the links, the init function is called
    init(ps.toList)

    val links = for (((p, i), id) <- ps.zipWithIndex.zip(ids)) yield {

      // Compare to every other paper
      val weights = for (((op, oi), oid) <- ps.zipWithIndex.zip(ids);
                                       w = getWeight(p, op, i, oi)
                                       if (oi != i) && (w > 1)) yield Link(oid, w)

      // Return a map from id to links
      (id -> weights.toList)
    }

    return links.toMap
  }
}
