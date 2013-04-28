package paper

abstract class DocSource {
  def getInfo[A <: DataItem](d : Document) : String
  def getLabel : String
}

object TalkDates extends DocSource {

  import scala.util.Random
  import java.util.Date
  import java.sql.Timestamp
  import java.util.Calendar

  // TODO: This is just a temporary implementation
  def getInfo[A <: DataItem](d : Document) : String = {

    // Get Calendar and Random
    var c = Calendar.getInstance
    var r = new Random

    // Set starting point as tomorrow at 8
    c.add(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY,8)
    c.set(Calendar.MINUTE,0)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    // Now add between zero and 6 hours
    c.add(Calendar.HOUR, (r.nextDouble * 7).toInt)
    // Add between 0 and 5 days
    c.add(Calendar.DAY_OF_MONTH, (r.nextDouble * 6).toInt)

    // Get a timeStamp
    var t = new Timestamp(c.getTime.getTime).getTime.toString

    //var n = new Timestamp(new Date().getTime).getTime
    //var r = (n + (new Random().nextDouble * (60*60*24*4*1000)).toLong).toString
    
    return t
  }

  def getLabel : String = "date"
}


// Adds the link of the pdf to the paper
object PdfLink extends DocSource {
  import scala.util.Random

  def getInfo[A <: DataItem](d : Document) : String = {

    var f : String = d.meta("file")
    var pdf : String = f.takeWhile(_!='.').concat(".pdf")
    return pdf;
  }

  def getLabel : String = "pdf"
}


object TalkRooms extends DocSource {
  import scala.util.Random

  // TODO: This is also just a temporary thing
  def getInfo[A <: DataItem](d : Document) : String = {
    
    // Return a random room between 1 and 10 
    return (new Random().nextDouble * 10).toInt.toString
  }

  def getLabel : String = "room"
}


/** Extend paper loops through a list of sources. Each source implements the
 * interface paperSource and provides two methods: getLabel and getInfo.
 * Get label returns the map label, while getInfo returns the particular information
 */
trait ExtendPaper {

  def extend(document : Document, sources : List[DocSource]) : Document = {
    var result : Document = document

    // For each source, check if it's already added, and if not, add it
    for (s <- sources if !document.hasMeta(s.getLabel)) {
      result = result.setMeta((s.getLabel -> s.getInfo(document)))
    }

    // return result
    result
  }

}
