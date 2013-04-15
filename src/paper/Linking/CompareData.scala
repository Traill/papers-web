package paper
import java.io._
import scala.io.Source

abstract trait CompareData {

  // If there is anything to be initialized it is done here
  def init(papers : List[DataItem]) : Unit = ()

  // Returns a weight between 0 and 100
  def getWeight(p1 : DataItem, p2 : DataItem, i1 : Int, i2 : Int) : Int

  // Creates a list of links
  def makeLinks(data : Map[String, DataItem]) : Map[String, List[Link]] = {

    // We need an indexed version of the DataItems
    val (ids, ds) = data.unzip

    // Before we make the links, the init function is called
    init(ds.toList)

    val links = (for (((d, i), id) <- ds.zipWithIndex.zip(ids)) yield {

      // Compare to every other dataitem
      val weights = for (((op, oi), oid) <- ds.zipWithIndex.zip(ids);
                                       w = getWeight(d, op, i, oi)
                                       if (oi != i) && (w > 1)) yield Link(oid, w)

      // Return a map from id to links
      (id -> weights.toList)

    }).toMap

    return links
  }
}
