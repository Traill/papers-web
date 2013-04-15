package paper


trait BagOfWords extends CompareData {

    var matrixOfWeights : Array[Array[Int]] = Array()

    override def init(data : List[DataItem]) : Unit = { 
      matrixOfWeights = getMatrixOfScores(data) 
    }

    def getWeight(p1 : DataItem, p2 : DataItem, i1 : Int, i2 : Int) : Int = getScores(matrixOfWeights, i1)(i2)

	def getScores(matrixOfScores: Array[Array[Int]], column: Int): List[Int] ={
	  
	  val matrixOfScoresTranspose = matrixOfScores.transpose
	  
	  return matrixOfScoresTranspose(column).toList

	}

	def getMatrixOfScores(data: List[DataItem]): Array[Array[Int]] ={
		val datasetSize = data.length
		//STEP 1: preprocess the data:
		val (textsList,counts) = preprocessTexts(data)
		//STEP 2: Build dictionary:
		val dictionary = buildDictionary(textsList, datasetSize)
		
		// we compute the array of scores for the vectors of words for every document
		val tfidfArray = Array.fill(dictionary.length,datasetSize)(0.0)
		
		//STEP 3: Computing tfidf with tfidf function:
		for (i <- 0 to dictionary.length -1){
			for (j <- 0 to datasetSize -1){
				//compute tfidf value for word i and document j
				tfidfArray(i)(j) = tfidf(dictionary(i),j,datasetSize,counts)
			}
		}
			
		//convert matrix to string to export it
		val mat = tfidfArray.deep.mkString("\n")

		//once we have the scores we can compute the absolute distance between data and classify them
		//This is performed computing a scalar product on the score vectors for every document
		//Computation might take some time
			
		//STEP 4: Computing cosine similarity:		
		val cosineSimilarity = computeCosineSimilarity(datasetSize, tfidfArray)	
		//check if rounding is done correctly
		val maximalWeight = cosineSimilarity.flatten.max
		//Normalize scores up to 100
		val normalizedCosSimilarity = cosineSimilarity.map(col =>{	
										col.map(weight =>  ((weight*100)/maximalWeight).toInt)
										})
		//return array of scores
					
		normalizedCosSimilarity
		//(i,j) of scalarProduct represents the scalar product of document i and document j. Now we have
		// to sort it in order in a list to return the closest documents to a given document
		//we have weights (higher weight/score) means being closer document-to-document wise
	}

	def preprocessTexts(data: List[DataItem]): (List[java.lang.String], Array[Map[java.lang.String,Int]]) ={
		var text = new Array[java.lang.String](data.length)
		val occurences = new Array[Map[java.lang.String,Array[java.lang.String]]](data.length)
		//now we want to have a map between words and the number of occurences
		//create an array for easier manipulation
		val counts = new Array[Map[java.lang.String,Int]](data.length)		
		//Create an array of lists to store all different lists of keys:
		val countsList = new Array[List[java.lang.String]](data.length)
		//List holding all the list of strings of all the texts
		var textsList = List[java.lang.String]()
		//reading from every entry of the list:
		for (k <- 0 to data.length-1){
			text(k) = data(k).getBody.text	 		    
			//leave out unecessary characters from the analysis
			text(k) = clean(text(k))
			    
			//Splitting the string into words to add stemming to every single word
			val splitString = text(k).split("\\s")
			var stemmedString = new Array[java.lang.String](splitString.length)
			var i = 0
			splitString	foreach	{e=>
			  						val a = breeze.text.analyze.PorterStemmer.apply(e)
		  							//There is still a blank space at the beginning of string (does not affect output)
		  							stemmedString(i) = a
		  							i+=1
		  							}

			// create a map of the keys of the text with their occurences
			counts(k) = stemmedString.groupBy(x=>x).mapValues(x=>x.length)

			//only working with keys for now, creating a list of keys for every text:
			countsList(k) = counts(k).keys.toList

			if(k == 0){
			textsList = countsList(k)
			}else{
			textsList = textsList ::: countsList(k)
			}
		}
		return (textsList, counts)
	}
	
	//Function to build the dictionary out of different texts concatenated in a list
	def buildDictionary(textsList: List[java.lang.String], datasetSize: Int) : List[java.lang.String] ={
		val dictionary = textsList.distinct.sortWith(_<_)
		return dictionary
	}
	
	def computeCosineSimilarity( datasetSize: Int, tfidfArray: Array[Array[Double]]) : Array[Array[Double]]={
		val scalarProduct = Array.fill(datasetSize,datasetSize)(0.0)
		val cosineSimilarity = Array.fill(datasetSize,datasetSize)(0.0)
		//transpose array to perform row Array operations instead of column based operations
		val tfidfTranspose = tfidfArray.transpose
		for (i <- 0 to datasetSize -1){
				for (j <- 0 to datasetSize -1){
					val normVectorI = math.sqrt(dotProduct(tfidfTranspose(i),tfidfTranspose(i)))
					val normVectorJ = math.sqrt(dotProduct(tfidfTranspose(j),tfidfTranspose(j)))
					if(i==j||normVectorI == 0 || normVectorJ == 0)
						cosineSimilarity(i)(j) = 0
					else{
						//May induce some computational time
						scalarProduct(i)(j) = dotProduct(tfidfTranspose(i), tfidfTranspose(j))				
						cosineSimilarity(i)(j) = scalarProduct(i)(j)/(normVectorI*normVectorJ)							
						//Here operations take cost of length O(dictionary length)
						//compute cosine similarity	
					}	
				}
		}
	  return cosineSimilarity
	}
	
	//Computing TF value:
	def tf(term: String, document: Int, counts: Array[Map[java.lang.String,Int]]): Double = {
		//Without normalisation
		if (counts(document).contains(term)){
			val freq = counts(document)(term)
			val normalizedFreq = freq
			return normalizedFreq
		}else{
			return 0.0
		}
	}

	//Computing IDF value
	def idf(term: String, datasetSize : Double, counts: Array[Map[java.lang.String,Int]]): Double = {
			// take the logarithm of the quotient of the number of documents by the documents where term t appears
			//convert appearances to a float (to avoid errors)
			var appearances : Double = 0.0
			counts foreach {x => 
			  					if (x.contains(term)){
			  					appearances += 1
							}
			}
			val a = math.log(datasetSize/appearances)  
			return a

	}

	def tfidf(term:String, document: Int, datasetSize : Double, counts: Array[Map[java.lang.String,Int]]) : Double = {
			//create tfidf matrix
			//tfidf = tf*idf
			
			val tfidf = tf(term,document,counts)*idf(term,datasetSize,counts)
			return tfidf
	}

	def exportMatrixToText(matrix: String) : Unit = {
		val file = new java.io.File("matrix.csv")
		val p = new java.io.PrintWriter(file)
		p.println(matrix)
		p.close
	}
	
	//defining scala product for array vector operations
	def dotProduct[T <% Double](as: Iterable[T], bs: Iterable[T]) = {
		require(as.size == bs.size)
		(for ((a, b) <- as zip bs) yield a * b) sum
	}

	//replace all characters of a string except for a-z or A-Z (replacing numbers) and finally _: 
	def clean(in : String) = { if (in == null) "" else in.replaceAll("[^A-Za-z_]", " ").toLowerCase
	}
	

}
