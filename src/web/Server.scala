package web

// Paper support
import paper._

// Unfiltered
import unfiltered.jetty._
import unfiltered.request._
import unfiltered.response._
import scala.io.Source
import java.io.PrintStream
import java.io.FileOutputStream

// JSON
import net.liftweb.json._


object Main {

  def main(args : Array[String]): Unit= {

    // Initialize data
    Data.init("isit2012")

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
  private var clusters : Map[String, String] = Map.empty

  // Graph data
  private var graphs : Map[String, String] = Map.empty

  // Nodes positions
  private var positions : Map[String, String] = Source.fromFile("positions.dat")
                                                 .getLines
                                                 .map( 
                                                        l => (l.substring(0, l.indexOf("###")) -> l.substring(l.indexOf("###")+3)) 
                                                      )
                                                 .toMap

  // Implicit val for JSON conversion
  private implicit val formats = DefaultFormats

  // Must be called to initialize all data from disk
  def init(path : String) : Unit = { 
    A = Analyzer.fromCache(path).load
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

  // Returns a cluster of size/type k
  def getClusters(k : String) : String = {
    if (!clusters.contains(k)) {

      val cs = for ((id, d) <- A.docs if d.cluster.contains(k)) yield (id -> d.cluster(k))
      clusters += (k -> Serialization.write(cs))
    }

    return clusters(k)
  }

  // Saves a particular graph
  def saveGraph(id : String, data : String) : Unit = {
    graphs += (id -> data)
    println(graphs)
  }

  def loadGraph(id : String) : String = graphs.getOrElse(id,"")


  // Save the position of the graph
  // There is still an id so that later on
  // we can save the position per user!
  def savePosition(id : String, data : String) : Unit = {
    positions += (id -> data)
    println("position saved")

    // write file
    var out = new PrintStream(new FileOutputStream("positions.dat"))
    positions.foreach( p  =>  out.println(p._1 + "###" + p._2)   )
  }

  // Reset the position of id 
  def resetPosition(id : String) : Unit = {
    positions = positions-(id);
    println("position reseted")

    // write file
    var out = new PrintStream(new FileOutputStream("positions.dat"))
    positions.foreach( p  =>  out.println(p._1 + "###" + p._2)   )
  }

  // Load the position of a graph
  def getPosition(id : String) : String = positions.getOrElse(id,"")
    


  // For debugging purposes
  def printIds : Unit = for ((id, _) <- A.docs) println(id)

  // Changes the path of the analyzer
  // We should check if the path is valid. Also this shouldn't be too exposed
  //def setPath(path : String) : Unit = Analyzer.initialize(path).load
}



object Server {

  import util.Properties

  def init : unfiltered.jetty.Http = {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = Properties.envOrElse("PORT", "8080").toInt
    println("starting on port: " + testPort)

    // Initialize Server
    val srv = unfiltered.jetty.Http(testPort).resources(resourceDir.toURI.toURL)

    // Run server
    srv.filter(Ajax).filter(Graph).filter(Page).filter(Schedule)
  }

}


