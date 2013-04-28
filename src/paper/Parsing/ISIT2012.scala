package paper

import java.io.File
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec

trait ISIT2012 extends XMLParser with PDFLoader {

  def parseDoc(doc : Document) : Option[Document] = {
    var paper : Option[Paper] = None
    val pdfName : String = Analyzer.filePath + doc.id + ".pdf"
    println(pdfName)
    val pdfFile : File = new File(pdfName)
    if (pdfFile.exists) paper = parseFile(doc, pdfToXML(pdfFile))
    return paper map { p => doc.setData(p) }
  }

}
