package paper

import net.liftweb.json._
import java.io.File


case class Graph(nodes : List[Node], edges : List[Edge])
case class Node(id : String, title : String, authors : List[String], room : String, time : String)
case class Edge(source : String, target : String, value : Int)

object Graph {

  // Number of edges we take from each node
  private val nEdges = 4


  // Creates a node given a document
  private def makeNode(id : String, d : Document) : Node = {
    Node(id, 
         d.paper.title.text, 
         d.paper.authors.map(_.name), 
         d.meta("room"), 
         d.meta("date"))
  }


  // Creates a list of edges given a document
  private def makeEdges(id : String, d : Document) : List[Edge] = {
    val ls = d.links.sortWith((l1,l2) => l1.weight > l2.weight)
    for (Link(to, w) <- ls.take(nEdges)) yield Edge(id, to, w)
  }


  // From a map of nodes, create a graph
  def make(docs : Map[String, Document]) : Graph = {

    // Make nodes
    val nodes = for ((id, d) <- docs) yield makeNode(id, d)

    // Make edges
    val edges = for ((id, d) <- docs; e <- makeEdges(id, d)) yield e

    // TODO: trim edges so there is only one edge between two nodes
    return Graph(nodes.toList, edges.toList)
  }


  // Save graph
  def print(graph : Graph, path : String) : Unit = {

    // Open printWriter
    val f = new java.io.File(path)
    val p = new java.io.PrintWriter(f)

    // Implicit values so we can write out a paper
    implicit val formats = DefaultFormats

    // Print as AMD with json
    var ret  = "define([], function () {\n\ndata = "
        ret += Serialization.writePretty(graph)
        ret += "\n\nreturn data;\n})"

    // End
    p.println(ret)
    p.close
  }
}


/** Escapes a raw string for use in HTML.*/
object Escape
{
	def apply(s: String) =
	{
		val out = new StringBuilder
		for(i <- 0 until s.length)
		{
			s.charAt(i) match
			{
				case '>' => out.append("&gt;")
				case '&' => out.append("&amp;")
				case '<' => out.append("&lt;")
				case '"' => out.append("&quot;")
				case '\n' => out.append(" ")
				case '\\' => out.append("\\\\")
				case c => out.append(c)
			}
		}
		out.toString
	}
}
