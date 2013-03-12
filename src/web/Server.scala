package web

// Paper support
import paper._

// Unfiltered
import unfiltered.netty._


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

  def init = {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = Properties.envOrElse("PORT", "8080").toInt
    println("starting on port: " + testPort)

    // Initialize Server
    val srv = unfiltered.netty.Http(testPort).chunked(1034332).resources(resourceDir.toURI.toURL)

    // Run server
    srv.handler(Ajax).handler(Graph).handler(Page).handler(Schedule)
  }
}


