package paper


object Main {
  def main(args : Array[String]): Unit= {
    // create analyzer
    val A : Analyzer = Analyzer(Map.empty)

    var t = A.initialize("resources/isit2012test").load.schedule("resources/isit2012test/schedule.xml").link

    lazy val c = Spectral(t)

  }


  // Reads in the options and converts them to a map of options
  def readOptions(s : String) : Map[String,Boolean] = {

    var options : Map[String,Boolean] = Map();

    if (s.length > 0 && s(0) == '-') {
      // Define options as always false, except
      options = Map().withDefaultValue(false)

      // Check for parsing
      if (s.contains('p')) options += ("parse" -> true)

      // Check for getting schedule
      if (s.contains('s')) options += ("xmlschedule" -> true)

      // Check for extending
      if (s.contains('e')) options += ("extend" -> true)

      // Check for linking
      if (s.contains('l')) options += ("link" -> true)

      // Check for linking
      if (s.contains('g')) options += ("graph" -> true)

      if(s.contains('h')) { 
        println("""How to call: Analyze [path] 
        [parameter]?\nPARAMETERS:\n\t-p : parsing\n\t-s : looks for xml 
        scheduler\n\t-c : compare\n\t-l : link\n\t-g : create graph\n\t-h 
        : shows this help page\n\tnothing : do everything"""); 
      }

    }

    else {
      // If no options are supplied we do everything by default
      options = Map().withDefaultValue(true)
    }

    return options
  }
}
