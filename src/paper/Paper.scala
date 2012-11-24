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

  // A series of functions to modify a paper
  def setMeta(p : Paper, m : (String, String)) : Paper = 
    Paper(p.id, p.index, p.title, p.authors, p.abstr, p.body, p.refs, p.meta + m, p.links)

  def setTitle(p : Paper, t : Title) : Paper =
    Paper(p.id, p.index, t, p.authors, p.abstr, p.body, p.refs, p.meta, p.links)

  def setAuthors(p : Paper, as : List[Author]) : Paper =
    Paper(p.id, p.index, p.title, as, p.abstr, p.body, p.refs, p.meta, p.links)

  def hasMeta(p : Paper, l : String) : Boolean = p.meta.contains(l)

  def setId(p : Paper, newId : Int) : Paper = 
    Paper(newId, p.index, p.title, p.authors, p.abstr, p.body, p.refs, p.meta, p.links)

  def setIndex(p : Paper, newIndex : Int) : Paper = 
    Paper(p.id, newIndex, p.title, p.authors, p.abstr, p.body, p.refs, p.meta, p.links)

  def setLinks(p : Paper, newLinks : List[Link]) : Paper = 
    Paper(p.id, p.index, p.title, p.authors, p.abstr, p.body, p.refs, p.meta, newLinks)
    
  def setAbstract(p : Paper, newAbstract : Abstract) : Paper = 
    Paper(p.id, p.index, p.title, p.authors, newAbstract, p.body, p.refs, p.meta, p.links)
    
  def setBody(p : Paper, newBody : Body) : Paper = 
    Paper(p.id, p.index, p.title, p.authors, p.abstr, newBody, p.refs, p.meta, p.links)
    
  def setReferences(p : Paper, newRefs : List[Reference]) : Paper = 
    Paper(p.id, p.index, p.title, p.authors, p.abstr, p.body, newRefs, p.meta, p.links)
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
                 val links :    List[Link]) {



}

case class Title(t: String)

case class Author(name: String)

case class Abstract(text: String)

case class Body(text: String)

case class Reference(authors: List[Author], title: Title)

case class Link(index : Int, weight : Int)
