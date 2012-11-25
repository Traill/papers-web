package paper

import scala.util.parsing.input._
import scala.collection.immutable.Stream
import scala.io.Source
import java.io.File



trait LoadPaper {

  def loadAndParse(location : String, parser : Parsers, loader : FileLoader) : List[Paper] = {
	println("BEGIN OF PARSING")
    
    // Get file handle of original file or directory
    val orig = new File(location)

    // Check that directory or file exists
    if (!orig.exists) sys.error("Something is wrong with the file or directory in the argument")

    // If exists, set name and file
    // In case it's a directory, let the file array contain all the files of the directory (regex utilization)
    val files : List[File]      = if(orig.isDirectory) SystemHelper.getFilesFromDirectory(orig) else List(orig)
    val ids : List[String]      = files.map(f => f.getName.split('.').head)


    // If postfix exists, try loading from cache
    val somePapers : List[Option[Paper]] = ids.map(id => Cache.load(id.toInt))

    // All papers that weren't loaded by cache are loaded by file
    val finalPapers = somePapers.zip(files).map(p => if (p._1 == None) loadFromFile(p._2, parser, loader) else p._1)

    // Filter papers for None's and set index
    val papers : List[Paper] = finalPapers.filter(p => p != None).zipWithIndex.map({case Pair(p,i) => p.get.setIndex(i) }).toList

    println("END OF PARSING")
    
    return papers
  }


  // Loads a paper from a text file and parses it. It has been modified in order to make loading and parsing flexible
  def loadFromFile(file : File, p : Parsers, loader: FileLoader) : Option[Paper] = {

    // Check if file is bad or contains non numerical values (in the name)
    //if (checkIfBad(file) || """[^0-9]+""".r.findFirstIn(SystemHelper.name(file.getName())).isDefined) return None
    
    val result = loader.loadFromFile(file, p)
    
    // If paper doesn't exist and didn't parse, let's not parse it again
    //if(result == None) return isBadFile(file)
    if (result != None) Cache.save(Paper.clean(result.get))          // Save and return
    
    return result
  }

}

