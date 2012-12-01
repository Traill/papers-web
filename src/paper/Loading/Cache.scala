package paper

import net.liftweb.json._
import java.io._
import scala.io.Source

object Cache {

  // Constants
  val dir = "cache" + File.separator
  val suffix = ".cache"


  // Save a document to cache
  def save(id : String, doc : Document) : String = {
    val json = Document.toJSON(doc)
    val filename = dir + id + suffix

    // Make sure directory exists
    val d = new File(dir)
    if (!d.exists) d.mkdirs

    // Make sure file exists
    val f = new File(filename)
    if(!f.exists) f.createNewFile

    // Write out JSON to file
    val w = new PrintWriter(f)
    w.println(json)

    // Close handles
    w.close

    return filename
  }


  // Given an id a paper will be loaded
  def load(id : String) : Option[Document] = {

    // Get file handle and check that it exists
    val filename = dir + id + suffix
    val file = new File(filename)
    if (!file.exists) return None

    // Now parse json
    val content : String = Source.fromFile(file).getLines.mkString
    val json = parse(content)
    Some(Document.fromJSON(json))
  }


  // remove cached file of paper or id
  def clean(id : Int) : Unit = {
    
    val filename = dir + id + suffix
    val file = new File(filename)
    if (file.exists) file.delete
  }
}