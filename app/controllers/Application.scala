package controllers
import play._
import play.api._
import play.api.mvc._
import java.io.File
import paper.Analyze
import generators._
import java.sql.Connection
import play.db.DB

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
		
		if(!emailAddress.isEmpty) {
			val mail = new MailSender("trailhead@epfl.ch", "Graph Personal Link",
			    "Your personal graph link is :\nTrailHead.epfl.ch/personalGraph/" + LinkGenerator.generateLink,
			    emailAddress.head);
			Ok(mail.send())
			//if(mail.send(emailAddress.head)) Redirect(routes.Application.index)
			//else Ok("Email not sent")
		} else Ok("Email empty")
	} else Ok("Body not present")
	
  }
  
  def getGraphData(link: String, field: String) = Action {
	val conn = DB.getConnection()
	// SQL injection !!!
	val result = conn.createStatement().executeQuery("SELECT " + field + " FROM tgraphs WHERE link = '" + link + "'")
	
	if(result.next()) {
	  Ok(result.getString(field))
	} else Ok("Graph not found")
  }
  
  def setGraphData(link: String, field: String, data: String) = Action {
	val conn = DB.getConnection()
	// SQL injection !!!
	val result = conn.createStatement().executeQuery("SELECT " + field + " FROM tgraphs WHERE link = '" + link + "'")
	
	if(result.next()) {
	  val result = conn.createStatement().executeQuery("UPDATE tgraph SET " + field + "=" + data + " WHERE link = '" + link + "'")
	  Ok("Row updated")
	} 
	else {
	  val result = conn.createStatement().executeQuery("INSERT INTO tgraphs (\""+ field + "\") VALUES (\"" + data +"\") WHERE link = '" + link + "'")
	  Ok("Row inserted")
	}
  }
  
  private def utf8URLDecode(url: String): String = {
	return """/%u([0-9a-f]{3,4})/i""".r.replaceAllIn(java.net.URLDecoder.decode(url.replace("\\00", "%u00"), "UTF-8"), "&#x\\1;")
  }
}