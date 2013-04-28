package paper

import net.liftweb.json._


case class Document(id : String,
                    data : DataItem, 
                    links : List[Link], 
                    meta : Map[String, String],
                    cluster : Map[String, Int]) {

  override def toString : String = "Doc[" + id + "]"

  def setId(newId : String) : Document = Document(newId, data, links, meta, cluster)
  def setData(d : DataItem) : Document = Document(id, d, links, meta, cluster)
  def setLinks(ls : List[Link]) : Document = Document(id, data, ls, meta, cluster)
  def setMeta(m : (String, String)) : Document = Document(id, data, links, meta + m, cluster)
  def setMeta(m : Map[String, String]) : Document = Document(id, data, links, meta ++ m, cluster)
  def setCluster(c : Map[String,Int]) : Document = Document(id, data, links, meta, cluster ++ c)
  def setCluster(c : (String,Int)) : Document = Document(id, data, links, meta, cluster + c)
  def hasMeta(l : String) : Boolean = meta.contains(l)
}

object Document {

  // Empty document for initialization
  val emptyDoc = Document("", NoData, List(), Map.empty, Map.empty)

  // Implicit values so we can write out a paper
  implicit val formats = DefaultFormats

  // Convert document to JSON
  def toJSON(d : Document) : String = Serialization.write(d)


  // Convert JSON to paper
  def fromJSON(json : String) : Document = Serialization.read[Document](json)

  // Try to clean the data a little
  //def clean(p : Paper) = Paper(p.title, p.authors.filter(a => a.name.length > 4), p.abstr, p.body, cleanRefs(p.refs), p.meta, p.links)
  def cleanRefs(refs : List[Reference]) = refs.map(r => Reference(r.authors.filter(a => a.name.stripMargin.length > 0), r.title))


}




