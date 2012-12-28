package web

import unfiltered.request._
import unfiltered.response._


// Plan for page view with template
object Page extends unfiltered.filter.Plan {

  def intent = {

    // Get page name and render it
    case Path(Seg("page" :: id :: Nil )) => { 

        val sourcetpl = scala.io.Source.fromFile("resources/templates/".concat(id))
        val template = sourcetpl.mkString
        sourcetpl.close

        ResponseString( template )

        

      }
    //case Path(Seg("page" :: Nil )) => { }

  }
}

