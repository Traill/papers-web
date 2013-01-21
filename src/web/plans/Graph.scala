package web

import unfiltered.request._
import unfiltered.Cookie
import unfiltered.response._

// Plan for ajax calls
object Graph extends unfiltered.filter.Plan {
  
  def intent = {

    case Path(Seg("graph" :: id :: w)) => loadGraph(id)
    case Params(Id(id)) => loadGraph(id)

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
