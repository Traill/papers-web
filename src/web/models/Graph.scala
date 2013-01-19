package web

object GraphModel {

  // Graph data
  private var graphs : Map[String, String] = Map.empty

  // Saves a particular graph
  def set(id : String, data : String) : Unit = {
    graphs += (id -> data)
  }


  // Loads a particular graph
  def get(id : String) : String = graphs.getOrElse(id,"")


  // Checks if we already have a particular id saved
  def contains(id : String) : Boolean = graphs.contains(id)
}
