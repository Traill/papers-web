package web

import unfiltered.request._
import unfiltered.response._
import net.liftweb.json.JsonDSL._
import sun.misc.BASE64Decoder

// Plan for ajax calls
object Ajax extends unfiltered.filter.Plan {
  
  def intent = {

    // Get abstract
    case Path(Seg("ajax" :: "abstract" :: id :: Nil)) => {
      PaperModel.getAbstract(id) match {
        case Some(t)  => Json(("success" -> true) ~ ("id" -> id) ~ ("abstract" -> t))
        case None     => Json(("success" -> false) ~ ("id" -> id))
      }
    }

    // Get nodes
    case Path(Seg("ajax" :: "nodes.js" :: Nil)) => {
      val json : String = PaperModel.getJsonNodes
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

    // Get edges
    case Path(Seg("ajax" :: "edges.js" :: Nil)) => {
      val json : String = PaperModel.getJsonEdges
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

    // Get cluser of size k
    case Path(Seg("ajax" :: "clusters" :: k :: Nil)) => {
      val json : String = PaperModel.getClusters(k)
      JsonContent ~> ResponseString(json)
    }

    // Save a graph
    case Path(Seg("ajax" :: "saveGraph" :: id :: Nil)) & Params(Data(data)) => {
      GraphModel.set(id, data)
      Json("success" -> true)
    }

    // Load a graph
    case Path(Seg("ajax" :: "loadGraph" :: id :: Nil)) => {
      val graph : String = GraphModel.get(id)
      JsonContent ~> ResponseString(graph)
    }

    // Check graph id
    case Path(Seg("ajax" :: "checkGraphId" :: id :: Nil)) => {
      Json(("taken" -> GraphModel.contains(id)) ~ ("name" -> id))
    }

    // Save position of the graph
    case Path(Seg("ajax" :: "savePos" :: id :: Nil)) & Params(params) => {
      PositionModel.set(id, params("data").head)
      Json(("success" -> true))
    }

    // Load a graph
    case Path(Seg("ajax" :: "loadPos" :: id :: Nil))  => {
      val pos : String = id match {
            case "default.js" => "define(function() { return ".concat(PositionModel.get("default") ).concat(" });")
            case _ => PositionModel.get(id)
      }
      JsonContent ~> ResponseString(pos)
    }

    // Reset a graph
    case Path(Seg("ajax" :: "resetPos" :: id :: Nil))  => {
      PositionModel.reset(id)
      Json(("success" -> true))
    }


  }

  // Extractor for getting the data param
  object Data extends Params.Extract("data", Params.first)
}
