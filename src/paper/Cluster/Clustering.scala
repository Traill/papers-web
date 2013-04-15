package paper

case class MeasureCluster[A <: DataItem](docs : Map[String, Document[A]], name : String) {

  // returns the clustering of the given name (louvain, spectral2, spectral3, etc)
  def getCluster(id : String) : Int = docs(id).cluster(name)
  def isInner(id1 : String, id2 : String) : Boolean = (getCluster(id1) == getCluster(id2))

  // Convenient format for Links
  val links : Map[(String,String),Int] = for ((id, d) <- docs; l <- d.links) yield ((id, l.id) -> l.weight)
  val (inner, outer) = links.partition { case ((id1, id2), _) => isInner(id1, id2) }

  val K : Double = links.values.sum

  // Cut size
  val cutSize : Double = (for ((_, weight) <- outer) yield weight).sum / K.toDouble

  // The sum of link values per vertex
  def Ks(id : String) : Double = docs(id).links.map(_.weight).sum.toDouble

  // The weight of links per cluster 
  def getWeight(l : ((String, String), Int)) : Double = l match {
    case ((id1, id2), w) => w - (Ks(id1) * Ks(id2) / (2.0 * K))
  }

  // def choose(n : Int, k : Int) : Int = {
  //   val n_fac = (1 to n) product
  //   val k_fac = (1 to k) product
  //   val n_min_k_fac = (1 to (n - k)) product
  //   n_fac / (k_fac * n_min_k_fac)
  // }

  // The modularity measure
  val modularity : Double = {

    // Return the community measure
    1.0/K * inner.map(getWeight(_)).sum
  }

  // // The surprise measure TODO: not finished
  // val surprise : Double = {
  //   val n = docs.size
  //   val m = links.size
  //   val avg = links.map(_.weight).sum.toDouble / m.toDouble
  //   val F = ((avg/2.0) * (n*n - n))
  //   0.0
  // }

}



