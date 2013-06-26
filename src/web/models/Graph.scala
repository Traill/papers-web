package web

// Import cache to write to database
import paper.Cache
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import scala.util.{Try,Success,Failure}

case class UserGraph(scheduled : List[String], filters : List[Filter], selected : String, spread : BigInt)
case class Filter(keywords : String, from : BigInt, to : BigInt, context : List[String], removed : Boolean, selected : Boolean)

object GraphModel {

  val collection : String = "custom_graphs"

  // Graph data
  private var graphs : Map[String, String] = Map.empty

  // Saves a particular graph
  def set(id : String, data : String) : Unit = fromJSON(data) match {
    case Some(g) => Cache.putItem(collection, id, g)
    case None => println("Problem saving graph: " + data)
  }

  // Loads a particular graph
  def get(id : String) : String = {
    val g : Option[UserGraph] = Cache.getItem[UserGraph](collection, id)
    g.map { toJSON(_).getOrElse("") } getOrElse("")
  }

  // Checks if we already have a particular id saved
  def contains(id : String) : Boolean = (get(id) != "")


  //******************************//
  //         json utils           //
  //******************************//

  // Implicit values so we can write out a paper
  implicit val formats = DefaultFormats

  // Convert graph to json
  def toJSON(g : UserGraph) : Option[String] = Try(write(g)).toOption

  // Convert JSON to paper
  def fromJSON(json : String) : Option[UserGraph] = Try(read[UserGraph](json)).toOption

  // Example saved graph:
  // {"scheduled":["203","257","1840","175","508","2309","658","1509"],
  //    "filters":[{"keywords":"polar","from":1360454400,"to":1360886340,"context":["title","authors"],"removed":false,"selected":true}]
  //    ,"selected":"1509"
  // }
}
