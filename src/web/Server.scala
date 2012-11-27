package web

import paper._
import unfiltered.jetty._
import unfiltered.request._
import unfiltered.response._

object Server {
  def main(args : Array[String]): Unit= {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = 8080

    // Initialize Server
    val srv = unfiltered.jetty.Http(testPort).resources(resourceDir.toURI.toURL)
    
    // Add plans
    //val plans : List[Plan] = List(Ajax)
    //val filtered = plans.foldLeft(src)(_ filter _)

    // Run server
    srv.run()
  }
}


// Plan for ajax calls
// object Ajax extends unfiltered.filter.Plan {
//   
//   def intent = {
//     // Get abstract
//     case Path(Seg("ajax" :: "abstract" :: id)) => ()
//   }
// }
