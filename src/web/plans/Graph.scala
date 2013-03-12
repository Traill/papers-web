package web

import unfiltered.request._
import unfiltered.Cookie
import unfiltered.response._
import unfiltered.netty._

// Plan for ajax calls
object Graph  extends async.Plan with ServerErrorResponse {
  
  def intent = {

    case req @ Path(Seg("graph" :: id :: w)) => req.respond(loadGraph(Some(id)))
    case req @ Params(Id(id)) => req.respond(loadGraph(Some(id)))
    case req @ Path(Seg(Nil)) => req.respond(loadGraph(None))

  }

  // Loads the graph and adds it as a cookie
  def loadGraph(id : Option[String]) = {
    val graph = id.map(GraphModel.get(_))
    val index = scala.io.Source.fromFile("resources/index.html").mkString
    if (graph == None) { 
      HtmlContent ~> ResponseString(index)
    }
    else {
      println(graph)
      SetCookies(Cookie("graph", graph.getOrElse(""))) ~> HtmlContent ~> ResponseString(index)
    }
  }


  // Extractor for getting an id param
  object Id extends Params.Extract("id", Params.first)
}
