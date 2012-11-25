package paper

import java.io.File
import scala.io.Source

case class Analyzer(docs : Map[String, Document]) {


  // Set directory where the pdf's are located
  def initalize(location : String) : Analyzer = {

    // Utility function for getting a document
    val empty : Map[String,String] = Map.empty
    def doc(f : File) = Document(Paper.empty, f, List(), empty)

    // Create new Analyze object
    val ds = Analyzer.getFiles(location).map(f => (f._1 -> doc(f._2)))
    return Analyzer(docs)
  }


  // Parse a paper
  def parse : Analyzer = { 

    val empty : Map[String,String] = Map.empty
    def doc(f : File) = Document(toPaper(Analyzer.load(f)), f, List(), empty)

    def toPaper(s : Source) = Analyzer.parse(s) match {
      case Some(p)  => p
      case None     => Paper.empty
    }

    // Parse every document
    val ds = docs.map(d => (d._1 -> doc(d._2.file)))

    // Filter every document that was correctly parsed
    val es = ds.filter(_._2.paper != Paper.empty)
    return Analyzer(es)
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
