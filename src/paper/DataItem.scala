package paper

trait DataItem {
  def getTitle : Title;
  def getPeople : List[Person]
  def getBody : Body
}

object NoData extends DataItem {
  def getTitle : Title = Title("")
  def getPeople : List[Person] = List()
  def getBody : Body = Body("")
  def toPaper = Paper(getTitle, getPeople, Abstract(""), getBody, List())
}

case class JustText(text : String) extends DataItem {
  def getTitle : Title = Title("")
  def getPeople : List[Person] = List()
  def getBody : Body = Body(text)
  def toPaper = Paper(getTitle, getPeople, Abstract(""), getBody, List())
}


case class Title(text: String)

case class Person(name: String)

case class Abstract(text: String)

case class Body(text: String)

case class Reference(authors: List[Person], title: Title)

case class Link(id : String, weight : Int)


/*
 * The datatype used for myEdu
 */
case class Course(title:    Title,
                  profs:    List[Person],
                  desc:    Body) extends DataItem {

  // Getters as defined in trait
  def getTitle : Title = title
  def getPeople : List[Person] = profs
  def getBody : Body = desc
}


/*
 * The datatype used for ISIT and ITA
 */
case class Paper(title:     Title, 
                 authors:   List[Person], 
                 abstr:     Abstract, 
                 body:      Body, 
                 refs:      List[Reference]) extends DataItem {

  override def toString : String = "Paper: " + title

  // Getters as defined in trait
  def getTitle : Title = title
  def getPeople : List[Person] = authors
  def getBody : Body = body

  // Some setters to make life easier
  def setTitle(t : String) : Paper =
    Paper(Title(t), authors, abstr, body, refs)
  def setTitle(t : Title) : Paper =
    Paper(t, authors, abstr, body, refs)

  def setAuthors(as : List[Person]) : Paper =
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



