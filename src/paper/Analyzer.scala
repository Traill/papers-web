package paper

import java.io.File
import scala.io.Source

case class Analyzer(docs : Map[String, Document]) extends GetFiles
                                                     with PDFLoader
                                                     with XMLParser 
                                                     with ExtendPaper
                                                     with BagOfWords
                                                     with XMLScheduleParser {


  /**
   * Set directory where the pdf's are located
   */
  def initialize(path : String) : Analyzer = {

    // Utility function for getting a document
    def doc(id : String, f : File) = Document.emptyDoc.setFile(f).setId(id)

    // Create new Analyze object
    val ds = for ((id, f) <- getFiles(path)) yield (id -> doc(id, f))
    return Analyzer(ds)
  }


  /**
   * Parse a paper
   */
  def parse(doc : Document) : Document = {

    def toPaper(f : File) = parseFile(pdfToXML(f)) match {
      case Some(p)  => p
      case None     => doc.paper
    }

    // Parse the paper linked to in the document
    doc.setPaper(toPaper(doc.file))
  }


  /**
   * Parse all documents
   */
  def parse : Analyzer = { 

    val ds = for ((id, d) <- docs; n = parse(d)
                        if (n.paper != Document.emptyPaper)) yield (id -> n)

    return Analyzer(ds)
  }


  /**
   * Links all the papers
   */
  def link : Analyzer = {
    
    // Get a map of papers and pass it to makeLinks
    val ps = for ((id, d) <- docs if (d.paper != Document.emptyPaper)) yield (id -> d.paper)
    val links = makeLinks(ps)

    // Now add links to each document
    val ds = for ((id, d) <- docs) yield (id -> d.setLinks(links(id)))

    return Analyzer(ds)
  }


  /**
   * Adds data from a schedule to all papers
   */
  def schedule(path : String) : Analyzer = {

    // get map of values
    val s = getXMLSchedule(path)

    // For each paper add these values to the corresponding paper
    val ds = for ((id, d) <- docs) yield (id -> d.setMeta(s.getOrElse(id,Map.empty)))

    return Analyzer(ds)
  }


  /**
   * Save to cache
   */
  def save : Analyzer = {

    // Save all documents
    for ((id, d) <- docs) Cache.save(id, d)

    return this
  }


  /**
   * Load from cache and if document isn't found, parse it
   */
  def load : Analyzer = {

    // Load all documents
    val docOption = for ((id, _) <- docs) yield (id -> Cache.load(id))

    // Parse all those that weren't found
    val ds = for ((id, d) <- docs) yield { 
      if (docOption(id) == None) (id -> parse(d))
      else (id -> docOption(id).get)
    }

    return Analyzer(ds)
  }


  /**
   * Output graph.js with a full graph of the data
   */
  def graph(path : String) : Analyzer = {

    // Make graph
    val graph = Graph.make(docs)

    // Print graph
    Graph.print(graph, path)

    return this
  }


  /**
   * Returns a paper from the collection
   */
  def get(id : String) : Option[Document] = docs.get(id)
}
