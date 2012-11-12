package generators
import java.util.Random;

object LinkGenerator {
	private val randomGenerator: Random = new Random()

	private def generate(max: Int, base: Int): Char = (randomGenerator.nextInt(max) + base).asInstanceOf[Char]
		
	def generateLink: String = {
		
		var link: String = ""
		var i = 0
		val charTypes = Array[(Int, Int)]((10, 48), (26, 65), (26, 97))
		
		while(i < 15) {
		  val typeT: (Int, Int) = charTypes(randomGenerator.nextInt(3))
		  
		  link = link + generate(typeT._1, typeT._2)
		  i = i + 1
		}
		
		link
	}
}