package web

import unfiltered.request._
import unfiltered.response._
import net.liftweb.json.JsonDSL._
import unfiltered.netty._
import sun.misc.BASE64Decoder

// Plan for ajax calls
object Ajax extends async.Plan with ServerErrorResponse {
  
  def intent = {

    // Get abstract
    case req @ Path(Seg("ajax" :: "abstract" :: id :: Nil)) => {
      PaperModel.getAbstract(id) match {
        case Some(t)  => req.respond(Json(("success" -> true) ~ ("id" -> id) ~ ("abstract" -> t)))
        case None     => req.respond(Json(("success" -> false) ~ ("id" -> id)))
      }
    }

    // Get nodes
    case req @ Path(Seg("ajax" :: "nodes.js" :: Nil)) => {
      val json : String = PaperModel.getJsonNodes
      req.respond(JsonContent ~> ResponseString("define(function() { return " + json + "; })"))
    }

    // Get edges
    case req @ Path(Seg("ajax" :: "edges.js" :: Nil)) => {
      val json : String = PaperModel.getJsonEdges
      req.respond(JsonContent ~> ResponseString("define(function() { return " + json + "; })"))
    }

    // Get cluser of size k
    case req @ Path(Seg("ajax" :: "clusters" :: k :: Nil)) => {
      val json : String = PaperModel.getClusters(k)
      req.respond(JsonContent ~> ResponseString(json))
    }

    // Save a graph
    case req @ Path(Seg("ajax" :: "saveGraph" :: id :: Nil)) & Params(Data(data)) => {
      GraphModel.set(id, data)
      req.respond(Json("success" -> true))
    }

    // Load a graph
    case req @ Path(Seg("ajax" :: "loadGraph" :: id :: Nil)) => {
      val graph : String = GraphModel.get(id)
      req.respond(JsonContent ~> ResponseString(graph))
    }

    // Check graph id
    case req @ Path(Seg("ajax" :: "checkGraphId" :: id :: Nil)) => {
      req.respond(Json(("taken" -> GraphModel.contains(id)) ~ ("name" -> id)))
    }

    // Save position of the graph
    case req @ Path(Seg("ajax" :: "savePos" :: id :: Nil)) & Params(params) => {
      PositionModel.set(id, params("data").head)
      req.respond(Json(("success" -> true)))
    }

    // Load a graph
    case req @ Path(Seg("ajax" :: "loadPos" :: id :: Nil))  => {
      val pos : String = id match {
            case "default.js" => "define(function() { return ".concat(PositionModel.get("default") ).concat(" });")
            case _ => PositionModel.get(id)
      }
      req.respond(JsonContent ~> ResponseString(pos))
    }

    // Reset a graph
    case req @ Path(Seg("ajax" :: "resetPos" :: id :: Nil))  => {
      PositionModel.reset(id)
      req.respond(Json(("success" -> true)))
    }


  }

  // Extractor for getting the data param
  object Data extends Params.Extract("data", Params.first)
}
