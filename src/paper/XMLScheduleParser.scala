package paper
import java.io.File

trait XMLScheduleParser {

  import scala.xml._
  import scala.collection.immutable.Map._

  // Overall function that loads the xml schedule and returns the papers with the extra data
  def getXMLSchedule(path : String) : Map[String, Map[String,String]] = {
	
    // Check if the schedule exists
    if (!(new File(path)).exists()) throw new Exception("No file called schedule.xml exists in papers path");
	
    // Parse schedule
    val xml : Map[String, Elem] = parse(path)

    // Create map from xml
    return xml.map(x => (x._1 -> toMap(x._2)))
  }


  // Function for taking care of parsing the xml
  private def parse(paperPos : String) : Map[String, Elem] = {

    // Load schedule file
    val schedule : Elem = XML.loadFile(paperPos)

    // Initialize Map
    var data : Map[String, Elem] = Map.empty

    // For each session record room, date, session
    (schedule \\ "session") foreach (session => {

      var date = (session \ "date").text
      var room = (session \ "room").text
      var sess = (session \ "code").text + ": " + (session \ "sessiontitle").text

      // Now for each paper record starttime, endtime, paperid, papertitle, abstract
      (session \\ "paper") foreach (paper => {
        // Create a hunk of xml containing all the data
        var xml = <session><date>{ date }</date><room>{ room }</room><sess>{ sess }</sess><info>{ paper }</info></session>
        var id = (paper \\ "paperid").text
        data = data + (id -> xml)
      })
    })

    // Return the map of all the data
    return data
  }


  // Transforms the xml to a map of the interesting values
  private def toMap(xml : Elem) : Map[String, String] = {

    val m : Map[String, String] = Map.empty
    return (m + ("date"        -> getDate(xml))
              + ("room"        -> getRoom(xml \\ "room"))
              + ("session"     -> (xml \\ "sess").text)
              + ("starttime"   -> (xml \\ "starttime").text)
              + ("endtime"     -> (xml \\ "endtime").text)
              + ("paperid"     -> (xml \\ "paperid").text)
              + ("sessionid"   -> (xml \\ "sessionid").text)
              + ("papertitle"  -> (xml \\ "papertitle").text)
              + ("authors"     -> getAuthors(xml \\ "authors").mkString(", ")))
  }

  // Converts an authors XML note to string
  private def getAuthors(authors : NodeSeq) : List[String] = {
    return (for (a <- (authors \ "author")) yield (a \\ "name").text).toList
  }

  private def getRoom(room : NodeSeq) : String = {
    room.text match {
      case "Track 1"              => "Kresge Rehearsal B (030)"
      case "Track 2"              => "Kresge Auditorium (109)"
      case "Track 3"              => "Stratton S. de P. Rico (202)"
      case "Track 4"              => "Stratton 20 Chimneys (306)"
      case "Track 5"              => "Kresge Little Theatre (035)"
      case "Track 6"              => "Kresge Rehearsal A (033)"
      case "Track 7"              => "Stratton (407)"
      case "Track 8"              => "Stratton (491)"
      case "Track 9"              => "Stratton West Lounge (201)"
    }
  }

  private def getDate(xml : Elem) : String = {
    import java.util.Calendar
    import java.sql.Timestamp

    var date = (xml \\ "date").text
    var time = (xml \\ "starttime").text

    var dateNum : Int = date.takeWhile(_.isDigit).toInt
    var hourNum : Int = time.split(':').head.toInt
    var minNum : Int = time.split(':').last.toInt

    // Get Calendar
    var c = Calendar.getInstance

    // Set starting point
    c.set(2012, 6, dateNum, hourNum, minNum)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    // Get a timeStamp
    var t = (new Timestamp(c.getTime.getTime).getTime).toString

    return t
  }

  private def formatAuthors(name : String) : String = {
    var result = ""
    var names = name.split(" ").filter(n => n.length > 0)
    if (names.length > 0) {
      result = names.init.filter(n => n.length > 0).map(n => n.head).mkString("",". ",". ") + names.last
    }
    return result
  }

}
