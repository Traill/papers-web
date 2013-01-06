package paper

// Cluster with:
//   String : id
//   Int:     size
//   Int:     group
case class MeasureCluster(docs : Map[String, Document], name : String) {

  // returns the clustering of the given name (louvain, spectral2, spectral3, etc)
  def getCluster(id : String) : Int = docs(id).cluster(name)
  def isInner(id1 : String, id2 : String) : Boolean = (getCluster(id1) == getCluster(id2))

  // Convenient format for Links
  val links : Map[(String,String),Int] = for ((id, d) <- docs; l <- d.links) yield ((id, l.id) -> l.weight)
  val (inner, outer) = links.partition { case ((id1, id2), _) => isInner(id1, id2) }

  // Cut size
  val cutSize : Double = (for ((_, weight) <- outer) yield weight).sum


  // The community measure
  val community : Double = {

    // The sum of link values
    val K : Int = links.values.sum

    // The links indexed by group
    val groups : Map[Int, Map[(String, String), Int]] = links.groupBy {
      case ((id,_),_) => getCluster(id) 
    }

    // The sum of link values per cluster
    val Ks : Map[Int, Int] = for ((g, m) <- groups) yield (g -> m.values.sum)

    // The weight of links per cluster 
    def getWeight(l : ((String, String), Int)) : Double = l match {
      case ((id1, id2), w) => w - Ks(getCluster(id1)) * Ks(getCluster(id2)) / (2.0 * K)
    }

    // Return the community measure
    1.0/K * inner.map(getWeight(_)).sum
  }
}



