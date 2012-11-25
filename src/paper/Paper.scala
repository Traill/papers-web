package paper

import net.liftweb.json._
import java.io.File


case class Document(paper : Paper, 
                    file : File, 
                    links : List[Link], 
                    meta : Map[String, String])

// TODO: delete meta
case class Paper(id :       Int, 
                 index :    Int,
                 title:     Title, 
                 authors:   List[Author], 
                 abstr:     Abstract, 
                 body:      Body, 
                 refs:      List[Reference], 
                 meta:      Map[String, String],
                 links :    List[Link]) extends AbstractPaper


case class Title(t: String)

case class Author(name: String)

case class Abstract(text: String)

case class Body(text: String)

case class Reference(authors: List[Author], title: Title)

case class Link(index : Int, weight : Int)

object Paper {

  // Empty paper for initialization
  val empty = Paper(0, 0, Title(""), List(), Abstract(""), Body(""), List(), Map.empty, List())

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


  // Convert JSON to paper
  def fromJSON(json : String) : Paper = fromJSON(parse(json))
  def fromJSON(json : JValue) : Paper = {

    def fields(fs : List[JField], p : Paper) : Paper = fs match {
      case Nil                                    => p
      case JField("id", JInt(t)) :: rest          => fields(rest, p.setId(t.toInt))
      case JField("index", JInt(t)) :: rest       => fields(rest, p.setIndex(t.toInt))
      case JField("title", JString(t)) :: rest    => fields(rest, p.setTitle(t))
      case JField("authors", JArray(t)) :: rest   => fields(rest, p.setAuthors(authors(t)))
      case JField("abstract", JString(t)) :: rest    => fields(rest, p.setAbstract(t))
      case JField("body", JString(t)) :: rest     => fields(rest, p.setBody(t))
      case JField("refs", JArray(t)) :: rest      => fields(rest, p.setReferences(refs(t)))
      case JField("meta", JObject(t)) :: rest     => fields(rest, meta(t, p))
      case JField("links", JArray(t)) :: rest     => fields(rest, p.setLinks(links(t)))
      case other                                  => throw new Exception("No match for field: " + other)
    }

    // Extracts Authors from JSON
    def authors(as : List[JValue]) : List[Author] = as match {
      case Nil                  => Nil
      case JString(t) :: rest   => Author(t) :: authors(rest)
      case otherwise            => throw new Exception("Authors have to be a JString")
    }

    // Extract References from JSON
    def refs(rs : List[JValue]) = rs.map(r => ref(r))
    def ref(r : JValue) = r match {
      case JObject(List(JField("authors", JArray(as)), JField("title", JString(title))))    => Reference(authors(as), Title(title))
      case otherwise                                                                        => throw new Exception("malformed link: " + otherwise)
    }

    // Extract Links from JSON
    def links(ls : List[JValue]) = ls.map(l => link(l))
    def link(l : JValue) = l match {
      case JObject(List(JField("id", JInt(id)), JField("w", JInt(weight)))) => Link(id.toInt, weight.toInt)
      case otherwise                                                        => throw new Exception("malformed link: " + otherwise)
    }

    // Extract Meta from JSON
    def meta(ms : List[JField], p : Paper) : Paper = ms match {
      case Nil                                  => p
      case JField(key, JString(value)) :: rest  => meta(rest, p.setMeta(key -> value))
      case otherwise                            => throw new Exception("Wrong meta format: " + otherwise)
    }

    // Check if we have a JObject
    json match {
      case JObject(fs)  => fields(fs, empty)
      case otherwise    => throw new Exception("Can't parse JSON " + otherwise)
    }
  }


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

  def setTitle(t : String) : Paper =
    Paper(id, index, Title(t), authors, abstr, body, refs, meta, links)
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
    
  def setAbstract(newAbstract : String) : Paper = 
    Paper(id, index, title, authors, Abstract(newAbstract), body, refs, meta, links)
  def setAbstract(newAbstract : Abstract) : Paper = 
    Paper(id, index, title, authors, newAbstract, body, refs, meta, links)
    
  def setBody(newBody : String) : Paper = 
    Paper(id, index, title, authors, abstr, Body(newBody), refs, meta, links)
  def setBody(newBody : Body) : Paper = 
    Paper(id, index, title, authors, abstr, newBody, refs, meta, links)
    
  def setReferences(newRefs : List[Reference]) : Paper = 
    Paper(id, index, title, authors, abstr, body, newRefs, meta, links)
}
