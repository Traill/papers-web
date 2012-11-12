package controllers
import play._
import play.api._
import play.api.mvc._
import java.io.File
import paper.Analyze
import generators._

object Application extends Controller { 
  
  def index = Action {
    Ok(views.html.index.render(""))
  }
  
  def generateSchedulePdf = Action { request =>
	val body = request.body.asFormUrlEncoded
	if(body != None) {
		val papers = body.get("papers[]").toList
		val abstractGet = body.get("abstract")
		
		val abstractVal = if(abstractGet.length == 1) abstractGet.toList.head.toInt else 0
		
		Ok(SchedulePdfGenerator.apply(papers, abstractVal))
	} else Ok("Something went wrong")
  }
  
  def generateTaskProcess(task: String, path: String) = Action {
	Ok(TaskProcessGenerator.apply(task, utf8URLDecode(path)))
  }
  
  def generatePersonalGraph(link: String) = Action {
	Ok(views.html.index.render(link))
  }
  
  def generateLink = Action { request =>
	val body = request.body.asFormUrlEncoded
	if(body != None) {
		val emailAddress = body.get("useremail").toList
		/*
		MailGenerator.send("trailhead@epfl.ch", 
							emailAddress.head,
							"Graph Personal Link",
							"Your personal graph link is :\nTrailHead.epfl.ch/personalGraph/" + LinkGenerator.generateLink)*/
	} 
	
	Redirect(routes.Application.index)
  }
  
  def getGraphData(link: String) = Action {
	Ok("")	// TODO
  }
  
  private def utf8URLDecode(url: String): String = {
	return """/%u([0-9a-f]{3,4})/i""".r.replaceAllIn(java.net.URLDecoder.decode(url.replace("\\00", "%u00"), "UTF-8"), "&#x\\1;")
  }
}