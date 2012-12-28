package web

import unfiltered.request._
import unfiltered.response._
import net.liftweb.json.JsonDSL._
import sun.misc.BASE64Decoder

// Add a new response for the ical format:
object IcalContent extends CharContentType("text/calendar")

 // Extract Post variable:
object Content extends Params.Extract("content", Params.first ~> Params.nonempty)



// Plan for ajax calls
object Ajax extends unfiltered.filter.Plan {
  
  def intent = {

    // Get abstract
    case Path(Seg("ajax" :: "abstract" :: id :: Nil)) => Data.getAbstract(id) match {
      case Some(t)  => Json(("success" -> true) ~ ("id" -> id) ~ ("abstract" -> t))
      case None     => Json(("success" -> false) ~ ("id" -> id))
    }

    // Get nodes
    case Path(Seg("ajax" :: "nodes.js" :: Nil)) => {
      val json : String = Data.getJsonNodes
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

    // Get edges
    case Path(Seg("ajax" :: "edges.js" :: Nil)) => {
      val json : String = Data.getJsonEdges
      JsonContent ~> ResponseString("define(function() { return " + json + "; })")
    }

    // Get cluser of size k
    case Path(Seg("ajax" :: "clusters" :: k :: Nil)) => {
      val json : String = Data.getClusters(k)
      JsonContent ~> ResponseString(json)
    }

    // Save a graph
    case Path(Seg("ajax" :: "save" :: id :: Nil)) & Params(params) => {
      Data.saveGraph(id, params("data").head)
      Json(("success" -> true))
    }

    // Load a graph
    case Path(Seg("ajax" :: "load" :: id :: Nil)) => {
      val graph : String = Data.loadGraph(id)
      JsonContent ~> ResponseString(graph)
    }

    // Get a pdf
    case POST(Path(Seg("pdf" :: pdf_name :: Nil)) & Params(Content(p))) => {
      var decoder = new BASE64Decoder()
      var decodedBytes = decoder.decodeBuffer(p);
      PdfContent ~> ResponseString(new String(decodedBytes) );
    }


    // Get a pdf
    case POST(Path(Seg("ical" :: ical_name :: Nil)) & Params(Content(p))) => {
      var decoder = new BASE64Decoder()
      var decodedBytes = decoder.decodeBuffer(p);
      IcalContent ~> ResponseString(new String(decodedBytes) );
    }

  }
}
