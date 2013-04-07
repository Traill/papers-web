package paper

import java.io.File
import scala.io.Source


// Compagnion object
object Analyzer extends GetFiles {

  var filePath : String = ""
  val resourceDir : String = "resources"
  val cacheDir : String = Cache.basedir
  var collection : String = ""


  // Initialize files from directory
  def initialize(path : String) : Analyzer = {

    // Set db collection to same name as the filepath
    collection = path

    // Update parent
    filePath = resourceDir + File.separator + path + File.separator

    // Return analyzer
    getAnalyzer
  }


  // Load files from cache
  def fromCache(c : String) : Analyzer = {

    // Set db collection to same name as the filepath
    collection = c

    Analyzer(Map.empty).load
  }


  // Function for initializing the analyzer
  private def getAnalyzer : Analyzer = {

    // Utility function for getting a document
    def doc(id : String, f : File) : Document = {
      println("Initializing " + id)
      Document.emptyDoc.setId(id)
    }

    // Create new Analyze object
    val ds = for ((id, f) <- getFiles(filePath)) yield (id -> doc(id, f))
    return Analyzer(ds)
  }

}


case class Analyzer(docs : Map[String, Document]) extends GetFiles
                                                     with ITA2013
                                                     with ExtendPaper
                                                     with BagOfWordsLSI
                                                     with XMLScheduleParser {

  /**
   * Parse all documents
   */
  def parse : Analyzer = { 

    val ds = for ((id, d) <- docs; n = parseDoc(d)
                        if (n != None)) yield (id -> n.get)

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
  def schedule(file : String) : Analyzer = {

    // get map of values
    val path = Analyzer.resourceDir + File.separator + Analyzer.collection + File.separator + file
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
    for ((id, d) <- docs) Cache.putItem[Document](Analyzer.collection, id, d)

    return this
  }


  /**
   * Load from cache
   */
  def load : Analyzer = {

    val docs = Cache.getQuery[Document](Analyzer.collection, Map.empty)
    val docMap = docs.map { d => (d.id -> d) } toMap

    return Analyzer(docMap)
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

    // Clusters organized by size, then id
    val clusters : Map[Int, Map[String, Int]] = Spectral(docs, k).cluster

    val ds = for((id, doc) <- docs) yield {

      // Create a list of groups by sizes for this particular id
      val cs = for ((size, m) <- clusters; (i, g) <- m if (id == i)) yield (size -> g)

      // Create a map of the new clusters consisting of clustername and group
      val groupings : Map[String, Int] = cs.map { case(s,g) => "spectral" + s -> g } 

      // Return the new document
      (id -> doc.setCluster(groupings))
    }

    return Analyzer(ds)
  }


  /**
   * Cluster the documents with louvain clustering
   */
  def louvain(treshold : Int = 20) : Analyzer = {

    val overTreshold = overNLinks(treshold)

    // clusters organized by id
    val clusters : Map[String, Int] = Louvain.cluster(Louvain.init(overTreshold.docs))

    val ds = for((id, d) <- docs) yield (id -> d.setCluster(("louvain" + treshold.toString) -> clusters(id)))

    return Analyzer(ds)
  }



  /**
   * Filters the links
   */
  def takeNLinks(n : Int) : Analyzer = {

    // Take the n strongest connections
    def takeN(n : Int)(ls : List[Link]) : List[Link] = ls.sortBy(_.weight).reverse.take(n)

    // Filter links
    val ds = for ((id, d) <- docs) yield (id -> d.setLinks(takeN(n)(d.links)))

    return Analyzer(ds)
  }



  /**
   * Filters the links
   */
  def overNLinks(n : Int) : Analyzer = {

    // Take only links over a certain treshold
    def overN(n : Int)(ls : List[Link]) : List[Link] = ls.filter(_.weight > n)

    // Filter links
    val ds = for ((id, d) <- docs) yield (id -> d.setLinks(overN(n)(d.links)))

    return Analyzer(ds)
  }



  /**
   * Returns a paper from the collection
   */
  def get(id : String) : Option[Document] = docs.get(id)
}
