package web

import unfiltered.request._
import unfiltered.Cookie
import unfiltered.response._
import unfiltered.netty._

// Plan for ajax calls
object Graph  extends async.Plan with ServerErrorResponse {
  
  def intent = {

    case req @ Path(Seg("graph" :: id :: w)) => req.respond(loadGraph(id))
    case req @ Params(Id(id)) => req.respond(loadGraph(id))

  }

  // Loads the graph and adds it as a cookie
  def loadGraph(id : String) = {
    val graph = GraphModel.get(id)
    val index = scala.io.Source.fromFile("resources/index.html").mkString
    println(graph)
    SetCookies(Cookie("graph", graph)) ~> HtmlContent ~> ResponseString(index)
  }


  // Extractor for getting an id param
  object Id extends Params.Extract("id", Params.first)
}
