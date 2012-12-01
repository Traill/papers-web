package paper

import java.io.File

trait GetFiles {


  // returns a list of files
  def getFiles(location : String) : Map[String, File] = {

    // Get file handle of original file or directory
    val orig = new File(location)

    // Check that directory or file exists
    if (!orig.exists) sys.error("Something is wrong with the file or directory in the argument")

    // If isDirectory isn't a directory
    if (!orig.isDirectory) return Map.empty + (baseName(orig) -> orig)

    // In case it's a directory, let the file array contain all the files of the directory (regex utilization)
    val files = orig.listFiles.toList.filter(_.getName.split('.').last == "pdf")

    // Convert to map
    return files.map(baseName(_)).zip(files).toMap
  }


  // Returns the basename of a file, i.e. the filename without the file extension
  private def baseName(f : File) : String = f.getName.split('.').head
}
