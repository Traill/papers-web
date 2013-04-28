package paper

import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import net.liftweb.json._

trait MyEdu extends XMLParser with PDFLoader
                                with CleanUnicode {

  // Takes a doc and parses it according to the information we have from ITA2013
  def parseDoc(doc : Document) : Option[Document] = {

    val path = Analyzer.filePath + doc.id + ".json"
    val json = parse(Source.fromFile(path).getLines.mkString)

    // Get paper and abstract if possible
    val course : Course = getCourse(json)

    // Add data to document if available
    Some(doc.setData(course))
  }

  private def getCourse(json : JValue) : Course = {

    // Get text of album
    val text = (json \ "en" \ "free_text" match { 
      case JObject(l) => l map { case JField(_, JString(s)) => s } 
    }) mkString(" ")

    // Get title
    val title = json \ "study_plan_entry" \ "title" match {
      case JString(t) => Title(t)
    }

    // Get profs
    val profs : List[Person] = json \ "en" \ "instructors"  match { 
      case JArray(l) => l.map { case JObject(_ :: JField(_,JString(n)) :: _) => Person(n) } 
    }

    Course(title, profs, Body(stripTags(text)))
  }

  private def stripTags(body : String) : String = {
    body.replace("[br/]"," ")
        .replace("[li]"," ")
  }

}


