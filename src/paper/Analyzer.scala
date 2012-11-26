package paper

import java.io.File
import scala.io.Source

case class Analyzer(docs : Map[String, Document]) {


  /**
   * Set directory where the pdf's are located
   */
  def initialize(path : String) : Analyzer = {

    // Utility function for getting a document
    def doc(f : File) = Document.emptyDoc.setFile(f)

    // Create new Analyze object
    val ds = for ((id, f) <- Analyzer.getFiles(path)) yield (id -> doc(f))
    return Analyzer(ds)
  }


  /**
   * Parse a paper
   */
  def parse : Analyzer = { 

    def toPaper(f : File) = Analyzer.parse(Analyzer.toXML(f)) match {
      case Some(p)  => p
      case None     => Document.emptyPaper
    }

    // Parse every document
    val ds = for ((id, d) <- docs; 
                        n = d.setPaper(toPaper(d.file))
                        if (n.paper != Document.emptyPaper)) yield (id -> n)

    return Analyzer(ds)
  }


  /**
   * Links all the papers
   */
  def link : Analyzer = {
    
    // Get a map of papers and pass it to makeLinks
    val ps = for ((id, d) <- docs) yield (id -> d.paper)
    val links = Analyzer.makeLinks(ps)

    // Now add links to each document
    val ds = for ((id, d) <- docs) yield (id -> d.setLinks(links(id)))

    return Analyzer(ds)
  }


  // Adds data from a schedule to all papers
  def schedule(path : String) : Analyzer = {

    // get map of values
    val s = Analyzer.getXMLSchedule(path)

    // For each paper add these values to the corresponding paper
    val ds = for ((id, d) <- docs) yield (id -> d.setMeta(s.getOrElse(id,Map.empty)))

    return Analyzer(ds)
  }


}

object Analyzer extends GetFiles
                   with PDFLoader
                   //with LoadPaper
                   with XMLParser 
                   with ExtendPaper
                   with BagOfWords
                   with XMLScheduleParser
