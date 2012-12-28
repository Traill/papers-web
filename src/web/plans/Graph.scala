package web

import unfiltered.request._
import unfiltered.Cookie
import unfiltered.response._

// Plan for ajax calls
object Graph extends unfiltered.filter.Plan {
  
  def intent = {

    case Params(Id(id)) => {
      val graph = Data.loadGraph(id)
      val index = scala.io.Source.fromFile("resources/index.html").mkString
      println(graph)
      SetCookies(Cookie("graph", graph)) ~> HtmlContent ~> ResponseString(index)
    }

  }

  // Extractor for getting an id param
  object Id extends Params.Extract("id", Params.first)
}
