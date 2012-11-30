package web

// Paper support
import paper._

// Unfiltered
import unfiltered.jetty._
import unfiltered.request._
import unfiltered.response._

// JSON
import net.liftweb.json._
import net.liftweb.json.JsonDSL._


object Main {

  def main(args : Array[String]): Unit= {

    // Initialize data
    Data.init("resources/isit2012test")

    // Fetch the server
    val srv = Server.init

    // Run the server
    srv.run
  }

}


// Right now this is just the papers
// Later it will also include links to graphs etc
object Data {

  private var A : Analyzer = Analyzer(Map.empty)

  // Must be called to initialize all data from disk
  def init(path : String) : Unit = { A = A.initialize(path).load }

  // Function for getting an abstract
  def getAbstract(id : String) : Option[String] = A.get(id).map(_.paper.abstr.text)

  // For debugging purposes
  def printIds : Unit = for ((id, _) <- A.docs) println(id)
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


// Plan for ajax calls
object Ajax extends unfiltered.filter.Plan {
  
  def intent = {

    // Get abstract
    case Path(Seg("ajax" :: "abstract" :: id :: Nil)) => Data.getAbstract(id) match {
      case Some(t)  => Json(("success" -> true) ~ ("id" -> id) ~ ("abstract" -> t))
      case None     => Json(("success" -> false) ~ ("id" -> id))
    }

  }
}
