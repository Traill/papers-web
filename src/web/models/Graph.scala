package web

// Import cache to write to database
import paper.Cache
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

case class UserGraph(scheduled : List[String], filters : List[Filter], selected : String)
case class Filter(keywords : String, from : BigInt, to : BigInt, context : List[String], removed : Boolean, selected : Boolean)

object GraphModel {

  val collection : String = "custom_graphs"

  // Graph data
  private var graphs : Map[String, String] = Map.empty

  // Saves a particular graph
  def set(id : String, data : String) : Unit = {
    println(data)
    Cache.putItem(collection, id, fromJSON(data))
  }

  // Loads a particular graph
  def get(id : String) : String = {
    val g : Option[UserGraph] = Cache.getItem[UserGraph](collection, id)
    println(g)
    g.map { toJSON(_) } getOrElse("")
  }

  // Checks if we already have a particular id saved
  def contains(id : String) : Boolean = (get(id) != "")


  //******************************//
  //         json utils           //
  //******************************//

  // Implicit values so we can write out a paper
  implicit val formats = DefaultFormats

  // Convert graph to json
  def toJSON(g : UserGraph) : String = write(g)

  // Convert JSON to paper
  def fromJSON(json : String) : UserGraph = read[UserGraph](json)

  // Example saved graph:
  // {"scheduled":["203","257","1840","175","508","2309","658","1509"],
  //    "filters":[{"keywords":"polar","from":1360454400,"to":1360886340,"context":["title","authors"],"removed":false,"selected":true}]
  //    ,"selected":"1509"
  // }
}
