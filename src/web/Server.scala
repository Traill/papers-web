package web

// Paper support
import paper._

// Unfiltered
import unfiltered.jetty._
import unfiltered.request._
import unfiltered.response._

// JSON
import net.liftweb.json._


object Main {

  def main(args : Array[String]): Unit= {

    // Initialize data
    Data.init("isit2012test")

    // Fetch the server
    val srv = Server.init

    // Run the server
    srv.run
  }

}


// Right now this is just the papers
// Later it will also include links to graphs etc
object Data {

  // The main datastructure
  private var A : Analyzer = Analyzer(Map.empty)

  // Json nodes and edges ready to be served
  private var nodes : String = ""
  private var edges : String = ""

  // Json clusters ready to be served
  private var clusters : Map[Int, String] = Map.empty

  // Implicit val for JSON conversion
  private implicit val formats = DefaultFormats

  // Must be called to initialize all data from disk
  def init(path : String) : Unit = { 
    A = Analyzer.initialize(path).load
  }

  // Function for getting an abstract
  def getAbstract(id : String) : Option[String] = A.get(id).map(_.paper.abstr.text)

  // Function for getting a json of all the nodes
  def getJsonNodes : String = {
    if (nodes == "") nodes = Serialization.write(A.graph.nodes)
    return nodes
  }

  // Function for getting a json of all the nodes
  def getJsonEdges : String = {
    if (edges == "") edges = Serialization.write(A.graph.edges)
    return edges
  }

  def getClusters(k : Int) : String = {
    if (!clusters.contains(k)) {

      val cs = for ((id, d) <- A.docs if d.cluster.contains(k)) yield (id -> d.cluster(k))
      clusters += (k -> Serialization.write(cs))
    }

    return clusters(k)
  }


  // For debugging purposes
  def printIds : Unit = for ((id, _) <- A.docs) println(id)

  // Changes the path of the analyzer
  // We should check if the path is valid. Also this shouldn't be too exposed
  //def setPath(path : String) : Unit = Analyzer.initialize(path).load
}



object Server {

  def init : unfiltered.jetty.Http = {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = 8080

    // Initialize Server
    val srv = unfiltered.jetty.Http(testPort).resources(resourceDir.toURI.toURL)
    
    // Run server
    srv.filter(Ajax)
  }

}


