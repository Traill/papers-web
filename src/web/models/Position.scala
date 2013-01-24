package web

object PositionModel {

  // Nodes positions
  private var positions : Map[String, String] = Map.empty

  // Save the position of the graph
  // There is still an id so that later on
  // we can save the position per user!
  def set(id : String, data : String) : Unit = {
    positions += (id -> data)
    println("position saved")
  }

  // Reset the position of id 
  def reset(id : String) : Unit = {
    positions = positions-(id);
  }

  // Load the position of a graph
  def get(id : String) : String = positions.getOrElse(id,"")
}
