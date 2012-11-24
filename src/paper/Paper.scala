package paper

import net.liftweb.json._

// Singleton with some utility functions
object Paper {


  // Convert Paper to Json
  def toJSON(p : Paper) = JObject(List(
                            JField("id", JInt(p.id)),
                            JField("title", JString(p.title.t)),
                            JField("authors", JArray(p.authors.map(a => JString(a.name)))),
                            JField("abstract", JString(p.abstr.text)),
                            JField("body", JString(p.body.text)),
                            JField("refs", JArray(p.refs.map(r => refToJSON(r)))),
                            JField("links", JArray(p.links.map(l => JObject(List(JField("id",JInt(l.index)), JField("w",JInt(l.weight))))))),
                            JField("meta", JObject(p.meta.map(m => JField(m._1, JString(m._2))).toList))
  ))
  def refToJSON(r : Reference) = JObject(List(
                               JField("authors", JArray(r.authors.map(a => JString(a.name)))),
                               JField("title", JString(r.title.t))
  ))


  // Try to clean the data a little
  def clean(p : Paper) = Paper(p.id, p.index, p.title, p.authors.filter(a => a.name.length > 4), p.abstr, p.body, cleanRefs(p.refs), p.meta, p.links)
  def cleanRefs(refs : List[Reference]) = refs.map(r => Reference(r.authors.filter(a => a.name.stripMargin.length > 0), r.title))


  // Get a list of all the distinct names in the paper authors and references
  def getDistinctNames(p : Paper) : List[String] = {
    val as = p.authors ::: p.refs.flatMap(r => r.authors)
    as.map(a => a.name).distinct
  }
}


// Abstract class for implementing a few helper methods
abstract class AbstractPaper {
  val id : Int;
  val index : Int;
  val title : Title;
  val authors : List[Author];
  val abstr : Abstract;
  val body : Body;
  val refs : List[Reference];
  val meta : Map[String, String];
  val links : List[Link];

  // A series of functions to modify a paper
  def setMeta(m : (String, String)) : Paper = 
    Paper(id, index, title, authors, abstr, body, refs, meta + m, links)

  def setTitle(t : Title) : Paper =
    Paper(id, index, t, authors, abstr, body, refs, meta, links)

  def setAuthors(as : List[Author]) : Paper =
    Paper(id, index, title, as, abstr, body, refs, meta, links)

  def hasMeta(l : String) : Boolean = meta.contains(l)

  def setId(newId : Int) : Paper = 
    Paper(newId, index, title, authors, abstr, body, refs, meta, links)

  def setIndex(newIndex : Int) : Paper = 
    Paper(id, newIndex, title, authors, abstr, body, refs, meta, links)

  def setLinks(newLinks : List[Link]) : Paper = 
    Paper(id, index, title, authors, abstr, body, refs, meta, newLinks)
    
  def setAbstract(newAbstract : Abstract) : Paper = 
    Paper(id, index, title, authors, newAbstract, body, refs, meta, links)
    
  def setBody(newBody : Body) : Paper = 
    Paper(id, index, title, authors, abstr, newBody, refs, meta, links)
    
  def setReferences(newRefs : List[Reference]) : Paper = 
    Paper(id, index, title, authors, abstr, body, newRefs, meta, links)
}



// Try to keep this immutable
case class Paper(val id :       Int, 
                 val index :    Int,
                 val title:     Title, 
                 val authors:   List[Author], 
                 val abstr:     Abstract, 
                 val body:      Body, 
                 val refs:      List[Reference], 
                 val meta:      Map[String, String],
                 val links :    List[Link]) extends AbstractPaper {



}

case class Title(t: String)

case class Author(name: String)

case class Abstract(text: String)

case class Body(text: String)

case class Reference(authors: List[Author], title: Title)

case class Link(index : Int, weight : Int)
