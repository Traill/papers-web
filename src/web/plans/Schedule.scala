package web

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import sun.misc.BASE64Decoder

// Add a new response for the ical format:
object IcalContent extends CharContentType("text/calendar")

 // Extract Post variable:
object Content extends Params.Extract("content", Params.first ~> Params.nonempty)



// Plan for ajax calls
object Schedule extends async.Plan with ServerErrorResponse {
  
  def intent = {

    // Get a pdf
    case req @ POST(Path(Seg("pdf" :: pdf_name :: Nil)) & Params(Content(p))) => {
      var decoder = new BASE64Decoder()
      var decodedBytes = decoder.decodeBuffer(p);
      req.respond(PdfContent ~> ResponseString(new String(decodedBytes) ))
    }


    // Get a pdf
    case req @ POST(Path(Seg("ical" :: ical_name :: Nil)) & Params(Content(p))) => {
      var decoder = new BASE64Decoder()
      var decodedBytes = decoder.decodeBuffer(p)
      req.respond(IcalContent ~> ResponseString(new String(decodedBytes) ))
    }
  }
}
