package paper
import scala.io.Source
import java.io.File
import scala.xml.XML
import scala.xml.Elem
import scala.xml.NodeSeq
import scala.xml.TypeSymbol
import scala.util.matching.Regex.MatchIterator
import sun.nio.cs.Unicode


trait XMLParser extends TitleExtractor1
                   with AuthorsExtractor1
                   with AbstractExtractor1
                   with BodyExtractor1
                   with CleanUnicode
                   with ReferencesExtractor1 {

   // The function for actually parsing a paper
   def parseFile(d : Document, f : File) : Option[Paper] = {
      val xml = getXMLObject(Source.fromFile(f))
      f.delete

	  if(xml == None) {
        println("Reading of Document " + d.id + " failed")
        None
      }
	  else {
          println("Reading of Document " + d.id + " succeeded")
		  val cleanPaper = NoData.toPaper
		  val xmlDocument = XMLObjectsManager.constructXMLDocument(xml.get, "\n")

		  // print
		  //xmlDocument.get.getParagraphs.foreach((p : XMLParagraph) => println(p.getText + "\n" + p.getOptionsValue + "\n" + p.getEnumerationFormat + "\n\n\n"))

		  if(xmlDocument == None) {
            println("Parsing of Document " + d.id + " failed")
            return None
          }
          println("Parsing of Document " + d.id + " succeeded")
		  val paper = extract(extractionOrder, (xmlDocument.get, Some(cleanPaper), xmlDocument.get.getParagraphs))

          return paper._2
	  }
   }

   private val extractionOrder: List[(Paper, XMLDocument, List[XMLParagraph]) => (List[XMLParagraph], Paper)] = List(extractTitle, extractAuthors, extractAbstract, extractBody, extractReferences)
	


   // This method returns the xml representation of the text contained in the Source object
   private def getXMLObject(in: Source): Option[Elem] = {
	  // String generation and illegal xml characters removing
	  val text : String = cleanUnicode(in.mkString)
      // This instruction is important, otherwise the xml file can't be deleted
      in.close
      Some(XML.loadString("""</?[bi]>""".r.replaceAllIn(text, "")))
   }


   // Method for references extraction following the extraction order
   private def extract(extractors: List[(Paper, XMLDocument, List[XMLParagraph]) => (List[XMLParagraph], Paper)], t : (XMLDocument, Option[Paper], List[XMLParagraph])): (XMLDocument, Option[Paper], List[XMLParagraph]) = {
	   def extract0(extractors: List[(Paper, XMLDocument, List[XMLParagraph]) => (List[XMLParagraph], Paper)], t : (XMLDocument, Option[Paper], List[XMLParagraph])): (XMLDocument, Option[Paper], List[XMLParagraph]) = {
		   if(extractors.length == 0) return t

	       val input = if(extractors.length == 1) t else extract0(extractors.tail, t)
		   val xml = input._1
		   val paper = input._2
		   val paragraphs = input._3

		   if(paper != None) {
			   // Calling the extraction method of the extractor
			   val extraction = extractors.head(paper.get, xml, paragraphs)
			   return (xml, Some(extraction._2), extraction._1)
		   }

		   (xml, None, paragraphs)
	   }

	   extract0(extractors.reverse, t)
   }

}
