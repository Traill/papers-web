package paper

import net.liftweb.json._


case class Document(id : String,
                    paper : Paper, 
                    links : List[Link], 
                    meta : Map[String, String],
                    cluster : Map[String, Int]) extends AbstractDocument

// TODO: delete meta
case class Paper(title:     Title, 
                 authors:   List[Author], 
                 abstr:     Abstract, 
                 body:      Body, 
                 refs:      List[Reference]) extends AbstractPaper


case class Title(text: String)

case class Author(name: String)

case class Abstract(text: String)

case class Body(text: String)

case class Reference(authors: List[Author], title: Title)

case class Link(id : String, weight : Int)

object Document {

  // Empty document for initialization
  val emptyDoc = Document("", emptyPaper, List(), Map.empty, Map.empty)

  // Empty paper for initialization
  val emptyPaper = Paper(Title(""), List(), Abstract(""), Body(""), List())

  // Implicit values so we can write out a paper
  implicit val formats = DefaultFormats

  // Convert document to JSON
  def toJSON(d : Document) : String = Serialization.write(d)


  // Convert JSON to paper
  def fromJSON(json : String) : Document = Serialization.read[Document](json)

  // Try to clean the data a little
  //def clean(p : Paper) = Paper(p.title, p.authors.filter(a => a.name.length > 4), p.abstr, p.body, cleanRefs(p.refs), p.meta, p.links)
  def cleanRefs(refs : List[Reference]) = refs.map(r => Reference(r.authors.filter(a => a.name.stripMargin.length > 0), r.title))


  // Get a list of all the distinct names in the paper authors and references
  def getDistinctNames(p : Paper) : List[String] = {
    val as = p.authors ::: p.refs.flatMap(r => r.authors)
    as.map(a => a.name).distinct
  }
}

abstract class AbstractDocument {
  val id : String
  val paper : Paper
  val links : List[Link]
  val meta : Map[String, String]
  val cluster : Map[String, Int]

  override def toString : String = "Doc ... "

  def setId(newId : String) : Document = Document(newId, paper, links, meta, cluster)
  def setPaper(p : Paper) : Document = Document(id, p, links, meta, cluster)
  def setLinks(ls : List[Link]) : Document = Document(id, paper, ls, meta, cluster)
  def setMeta(m : (String, String)) : Document = Document(id, paper, links, meta + m, cluster)
  def setMeta(m : Map[String, String]) : Document = Document(id, paper, links, meta ++ m, cluster)
  def setCluster(c : Map[String,Int]) : Document = Document(id, paper, links, meta, cluster ++ c)
  def setCluster(c : (String,Int)) : Document = Document(id, paper, links, meta, cluster + c)
  def hasMeta(l : String) : Boolean = meta.contains(l)
}


// Abstract class for implementing a few helper methods
abstract class AbstractPaper {
  val title : Title;
  val authors : List[Author];
  val abstr : Abstract;
  val body : Body;
  val refs : List[Reference];

  override def toString : String = "Paper: " + title

  def setTitle(t : String) : Paper =
    Paper(Title(t), authors, abstr, body, refs)
  def setTitle(t : Title) : Paper =
    Paper(t, authors, abstr, body, refs)

  def setAuthors(as : List[Author]) : Paper =
    Paper(title, as, abstr, body, refs)

  def setAbstract(newAbstract : String) : Paper = 
    Paper(title, authors, Abstract(newAbstract), body, refs)
  def setAbstract(newAbstract : Abstract) : Paper = 
    Paper(title, authors, newAbstract, body, refs)
    
  def setBody(newBody : String) : Paper = 
    Paper(title, authors, abstr, Body(newBody), refs)
  def setBody(newBody : Body) : Paper = 
    Paper(title, authors, abstr, newBody, refs)
    
  def setReferences(newRefs : List[Reference]) : Paper = 
    Paper(title, authors, abstr, body, newRefs)
}
