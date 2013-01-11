package paper

import java.io.File
import scala.io.Source


// Compagnion object
object Analyzer extends GetFiles {

  var path : String = ""
  val resourceDir : String = "resources"
  val cacheDir : String = Cache.basedir

  // Initialize files from directory
  def initialize(p : String) : Analyzer = {

    // Set the path of the analyzer
    path = p

    // Compile full path
    val fullPath = resourceDir + File.separator + path

    // Return analyzer
    getAnalyzer(fullPath, "pdf")
  }

  // Load files from cache
  def fromCache(p : String) : Analyzer = {

    // Set the path of the analyzer
    path = p

    // Compile full path
    val fullPath = cacheDir + File.separator + path

    // Return analyzer
    getAnalyzer(fullPath, Cache.suffix)

  }


  // Function for initializing the analyzer
  private def getAnalyzer(fullPath : String, suffix : String) : Analyzer = {

    // Utility function for getting a document
    def doc(id : String, f : File) : Document = {
      println("Initializing " + id)
      Document.emptyDoc.setFile(f).setId(id)
    }

    // Create new Analyze object
    val ds = for ((id, f) <- getFiles(fullPath, suffix)) yield (id -> doc(id, f))
    return Analyzer(ds)
  }

}


case class Analyzer(docs : Map[String, Document]) extends GetFiles
                                                     with PDFLoader
                                                     with XMLParser 
                                                     with ExtendPaper
                                                     with BagOfWordsLSI
                                                     with XMLScheduleParser {

  /**
   * Parse all documents
   */
  def parse : Analyzer = { 

    val ds = for ((id, d) <- docs; n = parse(d)
                        if (n.paper != Document.emptyPaper)) yield (id -> n)

    return Analyzer(ds)
  }


  /**
   * Parse a paper
   */
  def parse(doc : Document) : Document = {

    def toPaper(f : File) = parseFile(doc, pdfToXML(f)) match {
      case Some(p)  => p
      case None     => doc.paper
    }

    // Parse the paper linked to in the document
    doc.setPaper(toPaper(doc.file))
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
  def schedule(file : String) : Analyzer = {

    // get map of values
    val path = Analyzer.resourceDir + File.separator + Analyzer.path + File.separator + file
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
    for ((_, d) <- docs) Cache.save(d, Analyzer.path)

    return this
  }


  /**
   * Load from cache and if document isn't found, parse it
   */
  def load : Analyzer = {

    // Load all documents
    val docOption = for ((id, _) <- docs) yield (id -> Cache.load(id, Analyzer.path))

    // Parse all those that weren't found
    val ds = for ((id, d) <- docs) yield { 
      if (docOption(id) == None) (id -> parse(d))
      else (id -> docOption(id).get)
    }

    return Analyzer(ds)
  }


  /**
   * Generate a graph for use on the frontend
   */
  def graph : Graph = {

    // Make graph
    return Graph.make(docs)
  }


  /**
   * Cluster the documents with spectral clustering
   */
  def spectral(k : Int) : Analyzer = {

    val clusters : Seq[(String, (Int, Int))] = Spectral(docs, k).cluster

    val ds = for((id, doc) <- docs) yield {

      // This is not the most functional code in the world
      var newDoc = doc
      for ((i, (size, group)) <- clusters if (id == i)) { newDoc = newDoc.setCluster("spectral" + size -> group) } 

      (id -> newDoc)
    }

    return Analyzer(ds)
  }


  /**
   * Cluster the documents with louvain clustering
   */
  // def louvain : Analyzer = {

  //   val clusters : Map[String, Int] = Louvain(docs).cluster

  //   val ds = for((id, doc) <- docs) yield {

  //     // This is not the most functional code in the world
  //     var newDoc = doc
  //     for ((i, group) <- clusters if (id == i)) { newDoc = newDoc.setCluster("louvain" -> group) } 

  //     (id -> newDoc)
  //   }

  //   return Analyzer(ds)
  // }


  /**
   * Returns a paper from the collection
   */
  def get(id : String) : Option[Document] = docs.get(id)
}
