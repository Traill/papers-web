package paper

import net.liftweb.json._
import java.io._
import scala.io.Source

object Cache {

  // Constants
  val dir = "cache" + File.pathSeparator
  val suffix = ".cache"


  // Save a paper to cache
  def save(p : Paper) : String = {
    val json = Paper.toJSON(p)
    val filename = dir + p.id + suffix

    // Make sure directory exists
    val d = new File(dir)
    if (!d.exists) d.mkdirs

    // Make sure file exists
    val f = new File(filename)
    if(!f.exists) f.createNewFile

    // Write out JSON to file
    val w = new PrintWriter(f)
    w.println(pretty(render(json)))

    // Close handles
    w.close

    return filename
  }


  // Given an id a paper will be loaded
  def load(id : Int) : Option[Paper] = {

    // Get file handle and check that it exists
    val filename = dir + id + suffix
    val file = new File(filename)
    if (!file.exists) return None

    // Now parse json
    val content : String = Source.fromFile(file).getLines.mkString
    val json = parse(content)
    Some(Paper.fromJSON(json))
  }


  // remove cached file of paper or id
  def clean(p : Paper) : Unit = clean(p.id)
  def clean(id : Int) : Unit = {
    
    val filename = dir + id + suffix
    val file = new File(filename)
    if (file.exists) file.delete
  }
}
