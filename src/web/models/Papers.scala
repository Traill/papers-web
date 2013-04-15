package web

import paper._
import net.liftweb.json._

object PaperModel {

  // The main datastructure
  private var A : Analyzer[Paper] = Analyzer(Map.empty[String, Document[Paper]])

  // Json nodes and edges ready to be served
  private var nodes : String = ""
  private var edges : String = ""

  // Json clusters ready to be served
  private var clusters : Map[String, String] = Map.empty

  // Implicit val for JSON conversion
  private implicit val formats = DefaultFormats

  // Must be called to initialize all data from disk
  def init(path : String) : Unit = { 
    A = Analyzer.fromCache[Paper](path).load[Paper]
  }

  // Function for getting an abstract
  def getAbstract(id : String) : Option[String] = A.get(id).flatMap { d => d.data match {
      case Paper(_, _, abstr, _, _) => Some(abstr.text)
      case _                        => None
    }
  }

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
    if (!clusters.contains(k)) {

      val cs = for ((id, d) <- A.docs if d.cluster.contains(k)) yield (id -> d.cluster(k))
      clusters += (k -> Serialization.write(cs))
    }

    return clusters(k)
  }
}
