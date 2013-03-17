package paper

import net.liftweb.json._
import java.io._
import scala.io.Source
import scala.reflect.runtime.universe._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import scala.util.Properties
import com.mongodb.casbah.commons.conversions.scala._


object Cache {

  RegisterJodaTimeConversionHelpers()

  println(Properties.envOrNone("MONGOHQ_URL"));
  val MongoSetting(mongoDB) = Properties.envOrNone("MONGOHQ_URL")

  // Constants
  val basedir = "cache" + File.separator
  val suffix = "cache"


  // Save a document to cache
  def save(doc : Document, path : String) : String = {
    val json = Document.toJSON(doc)
    val dir = basedir + path + File.separator
    val filename = dir + doc.id + "." + suffix

    // Make sure cache directory exists
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
  def load(id : String, path : String) : Option[Document] = {

    // Get file handle and check that it exists
    val filename = basedir + path + File.separator + id + "." + suffix
    val file = new File(filename)
    if (!file.exists) return None

    // Now parse json
    val s : Source = Source.fromFile(file, "UTF-8")
    val content : String = s.mkString
    val json = parse(content)
    s.close
    Some(Document.fromJSON(json))
  }


  // remove cached file of paper or id
  def clean(id : Int, path : String) : Unit = {
    val filename = basedir + path + File.separator + id + "." + suffix
    val file = new File(filename)
    if (file.exists) file.delete
  }
}

object MongoSetting {

  val localDbName = "trailhead"

  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val db = MongoConnection(host, port.toInt)(dbName)
        db.authenticate(u,p)
        Some(db)
      case None =>
        Some(MongoConnection("localhost", 27017)(localDbName))
    }
  }
}
