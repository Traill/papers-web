package paper

import java.io.File
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec

trait ISIT2013 extends XMLParser with PDFLoader {

  var meta : Map[String, Map[String, String]] = Map.empty

  def parseDoc(doc : Document) : Option[Document] = {
    val pdfName : String = Analyzer.filePath + doc.id + ".pdf"
    println(pdfName)
    val pdfFile : Option[File] = getPdfOption(pdfName)
    return for (pdf <- pdfFile; 
                paper <- parseFile(doc, pdfToXML(pdf));
                withMeta <- setMeta(doc, paper))
               yield withMeta
  }

  private def getPdfOption(path : String) : Option[File] = {
    val f = new File(path)
    f.exists match {
      case true => Some(f)
      case false => None
    }
  }

  private def setMeta(doc : Document, paper : Paper) : Option[Document] = {
    def getAuthors(line : String) : List[Author] = {
      (line.split(";") map { l =>
        Author(l.split(",").map(_.trim).reverse.mkString(" "))
      }).toList
    }

    val csvPath = "resources/isit2013/isit2013.csv" // TODO: shouldn't be hardcoded

    // Fetch meta if we haven't done so already
    if (meta == Map.empty) meta = getMeta(csvPath)

    return meta.get(doc.id) map { meta =>
      val as = getAuthors(meta("authors"))
      val p = paper.setAbstract(meta("abstract")).setTitle(meta("title")).setAuthors(as)
      doc.setMeta(meta).setPaper(p)
    }
  }

  private def getMeta(path : String) : Map[String, Map[String, String]] = {

    val lines = Source.fromFile(path).getLines.drop(1)
    val m = for (l <- lines) yield {
      val data = chopLine(l)

      val id = data(0)
      val title = data(1)
      val abstr = data(2)
      val authors = data(3)
      val session = data(6)
      val sessionCode = data(7)
      val dateString = data(8)
      val sessionNumber = data(10)
      val timestamp = getDate(dateString.split(" ")(0), dateString.split(" ")(1), sessionNumber.toInt)
      id -> Map("title" -> title, "abstract" -> abstr, 
                "authors" -> authors, "session" -> session,
                "date" -> timestamp, "room" -> getRoom(sessionCode))
    }
    return m.toMap
  }


  private def getRoom(r : String) : String = r(3) match {
    case '1' => "Anadolu"
    case '2' => "Marmara"
    case '3' => "Dolmabahce A"
    case '4' => "Dolmabahce B"
    case '5' => "Dolmabahce C"
    case '6' => "Topkapi A"
    case '7' => "Topkapi B"
    case '8' => "Galata"
    case '9' => "Halic"
  }

  private def sanitizeLine(line : String) =
    line.replace("\"\"\",","'\",")
        .replace("\"\"\"","\"'")
        .replace("\"\"","'")

  private def chopLine(line : String) : List[String] = sanitizeLine(line) match {
    case ""                 => Nil
    case l if l(0) == '"'   => l.drop(1).takeWhile(_ != '\"') :: chopLine(l.drop(1).dropWhile(_ != '\"').drop(2))
    case l                  => l.takeWhile(_ != ',') :: chopLine(l.dropWhile(_ != ',').drop(1))
  }

  private def getDate(date : String, time : String, sessionNumber : Int) : String = {
    import java.util.Calendar
    import java.sql.Timestamp

    val dayNum : Int = date.split('-')(2).toInt
    val monthNum : Int = date.split('-')(1).toInt
    val yearNum : Int = date.split('-')(0).toInt
    val hourNum : Int = time.split(':').head.toInt
    val minNum : Int = time.split(':')(1).takeWhile(_.isDigit).toInt
    // Get Calendar
    var c = Calendar.getInstance

    val addMinutes = ((sessionNumber - 1) * 20) % 60
    val addHours = ((sessionNumber - 1) * 20) / 60


    // Set starting point
    c.set(yearNum, monthNum-1, dayNum, hourNum + addHours, minNum + addMinutes)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    // Get a timeStamp
    var t = (new Timestamp(c.getTime.getTime).getTime).toString

    return t
  }

}
