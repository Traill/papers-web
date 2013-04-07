package web

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._


// Plan for page view with template
object Page extends async.Plan with ServerErrorResponse {

  def intent = {

    // Get page name and render it
    case req @ Path(Seg("page" :: id :: Nil )) => { 

        val sourcetpl = scala.io.Source.fromFile("resources/templates/".concat(id))
        val template = sourcetpl.mkString
        sourcetpl.close

        req.respond(ResponseString( template ))
    }
  }
}

