package paper

import java.io.File
import scala.io.Source

case class Analyzer(docs : Map[String, Document]) {

  // Empty map
  val empty : Map[String,String] = Map.empty


  /**
   * Set directory where the pdf's are located
   */
  def initialize(location : String) : Analyzer = {

    // Utility function for getting a document
    def doc(f : File) = Document(Paper.empty, f, List(), empty)

    // Create new Analyze object
    val ds = for ((id, f) <- Analyzer.getFiles(location)) yield (id -> doc(f))
    return Analyzer(ds)
  }


  /**
   * Parse a paper
   */
  def parse : Analyzer = { 

    def doc(f : File) = Document(toPaper(f), f, List(), empty)

    def toPaper(f : File) = Analyzer.parse(Analyzer.toXML(f)) match {
      case Some(p)  => p
      case None     => Paper.empty
    }

    // Parse every document
    val ds = for ((id, d) <- docs; 
                        n = doc(d.file) if (n.paper != Paper.empty)) yield (id -> n)

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
    val ds = for ((id, d) <- docs) yield (id -> Document(d.paper, d.file, links(id), empty))

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
                   with Graphs



// class Analyzer extends Object with LoadPaper
//                               with ParsePaper 
//                               with ExtendPaper
//                               with BagOfWords
//                               with XMLScheduleParser
//                               with Graphs {
// 
//   // Set a limit in percent for when papers get an edge between them
//   val limit : Int = 1
// 
//   // Set sources we want to extend with
//   //val sources : List[PaperSource] = List(TalkDates, TalkRooms, PdfLink)
//   val sources : List[PaperSource] = List(PdfLink)
// 
//   // Analyze a paper
//   def analyze(paperPos: String, options: Map[String, Boolean]): List[Paper] = {
// 
//     var papers : List[Paper] = List();
// 
//     // Get a list of parsed papers
//     if (options("parse") == true) {
//       papers = loadAndParse(paperPos, new XMLParser(), XMLConverterLoader)
//     }
// 
//     // Mix in the schedule XML data
//     if (options("xmlschedule") == true) {
//       papers = getXMLSchedule(paperPos, papers)
//     }
// 
//     // Extend papers with tertiary data
//     if (options("extend") == true) {
//       papers = extend(paperPos, papers, sources)
//     }
// 
//     // Compare the papers individually
//     if (options("link") == true) {
//       papers = compareBoW(paperPos, papers, limit)
//     }
// 
//     // Create graph
//     if (options("graph") == true) {
//       val graph : Graph = getGraph(paperPos, papers)
// 
//       // Print graph to file 'data.json'
//       graph.save
//     }
// 
//     // Now return the papers as is
//     return papers
//   }
// }
