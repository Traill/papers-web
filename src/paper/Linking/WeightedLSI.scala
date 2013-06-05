package src.paper

import paper.BagOfWordsLSI

trait WeightedLSI extends BagOfWordsLSI{
  
  var matrixOfWeights : breeze.linalg.DenseMatrix[Int] = breeze.linalg.DenseMatrix.zeros[Int](0,0)

  override def init(papers : List[Paper]) : Unit = {
    matrixOfWeights = createTDMatrix(papers,50)
  }

  def getWeight(p1 : Paper, p2 : Paper, i1 : Int, i2 : Int) : Int = getScores(matrixOfWeights,i1).valueAt(i2)


  
  def getScores(matrixOfScores: breeze.linalg.DenseMatrix[Int], column: Int): DenseVector[Int] ={

	  val matrixOfScoresTranspose = matrixOfScores.t

	  return matrixOfScoresTranspose(::,column)

	}

	//Creating the matrix:
  override def createTDMatrix(papers: List[Paper], approximation: Int): breeze.linalg.DenseMatrix[Int] = {
		val datasetSize = papers.length

		//Initialisation of arrays
		//Array storing the different sources and the different texts
		
		//preprocess the papers:
		val (textsList,counts) = preprocessTexts(papers)
		
		//building dictionary:
		//find unique words in texts
		val dictionary = buildDictionary(textsList, datasetSize)
			
		// we compute the Matrix of scores for the vectors of words for every document
		//construct it as a vector to convert it as a Matrix
		val tfidfVector = new Array[Double](dictionary.length*datasetSize)
		var j = 0 
		for (i <- 0 to dictionary.length*datasetSize-1){
			//compute tfidf value for word i and document j
			//check if we have reached the length of the dictionary we change document and compute values
			if (i % dictionary.length == 0 && i != 0){
				j += 1	
			}
				
			tfidfVector(i) = tfidf(dictionary(i%dictionary.length),j,datasetSize,counts)			
		}

			//transform tfidf vector into a matrix to compute SVD:
			val termDocMatrix = new breeze.linalg.DenseMatrix[Double](dictionary.length,datasetSize,tfidfVector)

			//once having the termDocMatrix, compute the SVD and then compute cosine similarity on the new matrix:
			//SVD method returns simple arrays - need to convert them:
			//Dimensionality reduction:
			val (u,s,v) = svd(termDocMatrix)

			//converting s and v to 2 dimensional arrays:
			var vo = reconstructArray(v,termDocMatrix.cols)
			println(vo.length + " " + vo.transpose.length)
			var so = Array.ofDim[Double](termDocMatrix.rows,termDocMatrix.cols)
			println(so.length + " " + so.transpose.length)
			var count = 0
			var count2 = 0

			// put the vector s into a n*m matrix so form:
			s foreach{e=>	 
			  if(count2 <= termDocMatrix.cols-1){
					so(count)(count2) = e		
					count += 1
					count2 += 1			
			  		}
			}
			// SVD returns the first k values as the k highest values - assuming no need for indices:
			//select values of the indices in the given array s - indices are the 0 to k-1 first values:
			val emptyArray : Array[Array[Double]] = Array.empty
			//keeping k greatest singular values:
			val keptValues = selectElementsOf2dimArray(so,(0 to approximation-1).toList,emptyArray)
			var newKeptValues = Array.fill(keptValues.length,keptValues.length)(0.0)
			var arrayCounter = 0 

			val keptVTr = keptValues.transpose
			keptVTr foreach{e =>
			if (arrayCounter <= approximation-1){
				newKeptValues(arrayCounter) = e
				arrayCounter += 1
			}else{
			  Nil
			}
			  }
			val emptyArray2 : Array[Array[Double]] = Array.empty
			//Select k=approximation rows from Dt the document matrix where each column represents a document
			var newVo = selectElementsOf2dimArray(vo,(0 to approximation-1).toList,emptyArray2)

			val recomposedMatrix = newVo.transpose.flatten
			
			val documentConceptMatrix = new breeze.linalg.DenseMatrix[Double](approximation,termDocMatrix.cols,recomposedMatrix)
			//We take our s vector of singular values and convert it to a DenseVector
			val singularValuesVector = new DenseVector[Double](s.slice(0,approximation))
			//Create a diagonal matrix out of these values
			val singularValuesMatrix = diag(singularValuesVector)

			//Matrix reduction with an approximation app - if no matrix reduction, use old tfidf method:
			def matchApproximation(app: Int): breeze.linalg.DenseMatrix[Double] = app match{
			  //keep old method if no matrix reduction
			  case termDocMatrix.cols =>  termDocMatrix
			  case 0 => termDocMatrix
			  //Scale by singular values every concept
			  case y => singularValuesMatrix*documentConceptMatrix         
			}

			val newtermDocMatrix = matchApproximation(approximation)

			//compute cosine similarity:		
			val similarityMatrix = computeCosineSimilarity(datasetSize, newtermDocMatrix)
			//normalize weights from -100 to 100
			val maximalWeight = similarityMatrix.max
			val normalizedCosSimilarity = similarityMatrix.map(weight =>{
												((weight*100)/maximalWeight).toInt
															})

			return normalizedCosSimilarity
	}

}