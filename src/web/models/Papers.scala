package web

import paper._
import net.liftweb.json._

object PaperModel {

  // The main datastructure
  private var A : Analyzer = Analyzer(Map.empty)

  // Json nodes and edges ready to be served
  private var nodes : String = ""
  private var edges : String = ""

  // Json clusters ready to be served
  private var clusters : Map[String, String] = Map.empty

  // Implicit val for JSON conversion
  private implicit val formats = DefaultFormats

  // Must be called to initialize all data from disk
  def init(path : String) : Unit = { 
    A = Analyzer.fromCache(path).load
  }

  // Function for getting an abstract
  def getAbstract(id : String) : Option[String] = A.get(id).map(_.paper.abstr.text)

  // Function for getting a json of all the nodes
  def getJsonNodes : String = {
    if (nodes == "") nodes = Serialization.write(A.graph.nodes)
    return nodes
  }

  // Function for getting a json of all the nodes
  def getJsonEdges : String = {
    if (edges == "") edges = Serialization.write(A.graph.edges)
    return edges
  }

  // Returns a cluster of size/type k
  def getClusters(k : String) : String = {
    println(clusters)
    if (!clusters.contains(k)) {

      val cs = for ((id, d) <- A.docs if d.cluster.contains(k)) yield (id -> d.cluster(k))
      clusters += (k -> Serialization.write(cs))
    }

    return clusters(k)
  }
}
