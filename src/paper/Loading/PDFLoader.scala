package paper
import java.io.File
import scala.io.Source

trait PDFLoader {


  // Abstract load method to be implemented in sub classes
  def load(file : File) : Source = {
    Source.fromFile(convertToXML(file))
  }


  // The apps is on Windows
  private def isWindows: Boolean = sys.props.get("os.name") match {
    case Some(s) => """.*Windows.*""".r.findFirstIn(s.toString()).isDefined
    case None => false
  }


  // The apps is on Linux
  private def isLinux: Boolean = sys.props.get("os.name") match {
    case Some(s) => """.*Linux.*""".r.findFirstIn(s.toString()).isDefined
    case None => false
  }


  // Converts pdf file to XML
  private def convertToXML(file : File): File = {

    // The command, params and path used to process the pdf to xml
    val command = if (isWindows) "tools\\windows\\pdfToxmlConverter.exe" else "pdftohtml"
    val path = List(file.getAbsolutePath())
    val params = List("-xml", "-q", "-enc", "UTF-8")

    // Transform pdf to xml
    val process: Process = sys.runtime.exec((command :: params ::: path).toArray[String])

    // Waiting until the end of the command execution
    if(process.waitFor() != 0) throw new Exception("Can't convert pdf file to xml")

    // Return a fileHandle for the newly created xml file
    new File(file.getName.split('.').head + ".xml")
  }

}



