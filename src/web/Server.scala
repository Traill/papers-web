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
    PaperModel.init("ita2013")

    // Fetch the server
    val srv = Server.init

    // Run the server
    srv.run
  }

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


