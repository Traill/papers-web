package web

import unfiltered.request._
import unfiltered.response._
import sun.misc.BASE64Decoder

// Add a new response for the ical format:
object IcalContent extends CharContentType("text/calendar")

 // Extract Post variable:
object Content extends Params.Extract("content", Params.first ~> Params.nonempty)



// Plan for ajax calls
object Schedule extends unfiltered.filter.Plan {
  
  def intent = {

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
