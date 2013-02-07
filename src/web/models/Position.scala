package web

import scala.io.Source
import java.io.PrintStream
import java.io.FileOutputStream
import java.io.File

object PositionModel {

  // Nodes positions
  private var positions : Map[String, String] = Map.empty


  // Loads positions from file if they haven't been loaded already
  def getPositions : Map[String, String] = {

    if (positions == Map.empty) {
      // check if file exists
      val f = new File("positions.dat")
      if (f.exists) {
        positions = Source.fromFile(f)
                          .getLines
                          .map { l => 
                            (l.substring(0, l.indexOf("###")) -> l.substring(l.indexOf("###")+3)) 
                          } toMap
      }
    }

    return positions
  }

  // Save the position of the graph
  // There is still an id so that later on
  // we can save the position per user!
  def set(id : String, data : String) : Unit = {
    positions += (id -> data)
    println("position saved")

    // write file
    var out = new PrintStream(new FileOutputStream("positions.dat"))
    positions.foreach( p  =>  out.println(p._1 + "###" + p._2)   )
  }

  // Reset the position of id 
  def reset(id : String) : Unit = {
    positions = positions-(id);
    println("position reset")

    // write file
    var out = new PrintStream(new FileOutputStream("positions.dat"))
    positions.foreach( p  =>  out.println(p._1 + "###" + p._2)   )
  }

  // Load the position of a graph
  def get(id : String) : String = positions.getOrElse(id,"")
  // Save the position of the graph
  // There is still an id so that later on
  // we can save the position per user!

}
