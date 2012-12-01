package web

import unfiltered.request._
import unfiltered.response._
import net.liftweb.json.JsonDSL._

// Plan for ajax calls
object Ajax extends unfiltered.filter.Plan {
  
  def intent = {

    // Get abstract
    case Path(Seg("ajax" :: "abstract" :: id :: Nil)) => Data.getAbstract(id) match {
      case Some(t)  => Json(("success" -> true) ~ ("id" -> id) ~ ("abstract" -> t))
      case None     => Json(("success" -> false) ~ ("id" -> id))
    }

    case Path(Seg("ajax" :: "nodes.js" :: Nil)) => {
      val json : String = Data.getJsonNodes
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

    case Path(Seg("ajax" :: "edges.js" :: Nil)) => {
      val json : String = Data.getJsonEdges
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

  }
}
