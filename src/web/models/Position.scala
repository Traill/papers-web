package web

import paper.Cache
import scala.io.Source
import java.io.PrintStream
import java.io.FileOutputStream
import java.io.File

case class Positions(data : String)

object PositionModel {


  // Nodes positions
  private var collection = "positions"


  // Save the position of the graph
  def set(id : String, data : String) : Unit = {
    Cache.putItem[Positions](collection, id, Positions(data))
    println("position saved: " + id)
  }

  // Reset the position of id 
  def reset(id : String) : Unit = {
    Cache.putItem[Positions](collection, id, Positions(""))
    println("position reset")
  }

  // Load the position of a graph
  def get(id : String) : String = {
    println("position loaded " + id)
    val p : Option[Positions] = Cache.getItem[Positions](collection, id)
    p.getOrElse(Positions("")).data
  }

}
