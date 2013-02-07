package paper

import java.io.File
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec

trait ITA2013 extends XMLParser with PDFLoader
                                with CleanUnicode {

  // Takes a doc and parses it according to the information we have from ITA2013
  def parseDoc(doc : Document) : Option[Document] = {

    // Get paper and abstract if possible
    val pdf : Option[Paper] = getPdf(doc)
    val abstr : Option[String] = getAbstract(doc)

    // Update paper
    val paper = (pdf, abstr) match {
      case (None, None)         => None
      case (Some(p), None)      => Some(p)
      case (None, Some(a))      => Some(Document.emptyPaper.setAbstract(a))
      case (Some(p), Some(a))   => Some(p.setAbstract(a))
    }

    // Add additional information and return
    for (p <- paper) yield setInfo(doc, p)
  }

  // Sets info of paper according to information we have from ITA2013
  private def setInfo(doc : Document, paper : Paper) : Document = {
    val f = doc.file
    val info : List[String] = Source.fromFile(f).getLines.toList
    if (info.length < 8) throw new Exception("Corrupted paper file: " + f.getName)

    val authors = getAuthors(info(3))
    val title = info(2)
    val session = info(4).toInt
    val room = getRoom(session)
    val date = getDate(info(5).split(',')(0).trim, info(5).split(',')(1).trim)

    val p = (paper.setTitle(title)
                  .setAuthors(authors.toList))

    (doc.setPaper(p)
        .setMeta("room" -> room)
        .setMeta("date" -> date)
        .setMeta("session" -> session.toString))

  }

  private def getAuthors(authors : String) : List[Author] = {

    authors.split(';').toList.map { c => 

      // Discard the school information
      val name = c.takeWhile(_ != '(')

      // Reorder name to first last
      val firstlast : String = name.split(',').toList match {
        case last :: first :: Nil => first.trim + " " + last.trim
        case name :: Nil => name.mkString.trim
        case Nil => throw new Exception("Malformed author: " + authors)
      }

      // Return
      Author(firstlast)
    }
  }

  private def getDate(date : String, time : String) : String = {
    import java.util.Calendar
    import java.sql.Timestamp

    val dayNum : Int = date.split('/')(1).toInt
    val monthNum : Int = date.split('/')(0).toInt
    val yearNum : Int = date.split('/')(2).toInt
    val hour12Num : Int = time.split(':').head.toInt
    val minNum : Int = time.split(':').last.takeWhile(_.isDigit).toInt
    val hourNum : Int = time.split(':').last.dropWhile(_.isDigit) match {
      case "am" => hour12Num
      case "pm" => if (hour12Num == 12) { hour12Num } else { hour12Num + 12 }
    }

    // Get Calendar
    var c = Calendar.getInstance

    // Set starting point
    c.set(yearNum, monthNum-1, dayNum-1, hourNum, minNum)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    // Get a timeStamp
    var t = (new Timestamp(c.getTime.getTime).getTime).toString

    return t
  }


  private def getRoom(n : Int) : String = n match {
    case 1 => "The Cocatoo"
    case 2 => "The Mackaw"
    case 3 => "The Tookan"
    case 4 => "The Boardroom"
  }

  // Check if we have a pdf file
  private def getPdf(doc : Document) : Option[Paper] = {
    var paper : Option[Paper] = None
    val pdfName : String = doc.file.getParent + "/files/" + doc.id + ".pdf"
    val pdfFile : File = new File(pdfName)
    if (pdfFile.exists) paper = parseFile(doc, pdfToXML(pdfFile))
    return paper
  }

  // check if we have an abstract
  private def getAbstract(doc : Document) : Option[String] = {

    // Set up codecs
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Then get abstract
    var abstr : Option[String] = None
    val abstrName : String = doc.file.getParent + "/files/abstract_" + doc.id + ".txt"
    val abstrFile : File = new File(abstrName)
    if (abstrFile.exists) abstr = Some(cleanUnicode(Source.fromFile(abstrFile).getLines.mkString(" ")))
    return abstr
  }
}


