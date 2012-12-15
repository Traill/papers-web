package paper

object Louvain {

  // The first value
  var init : CommunityMap = CommunityMap(Map.empty)

  def init(docs : Map[String, Document]) : this.type = {
    val idMap : Map[String, Int] = for (((id, _), i) <- docs.zipWithIndex) yield (id -> i)
    def link(id : String, l : Link) = CommunityLink(Id(idMap(id)), Id(idMap(l.id)), l.weight)
    def doc(d : Document) = CommunityDoc(Id(idMap(d.id)), d.links.overN(10).map(link(d.id,_)))
    val cs : Map[Index, CommunityDoc] = for ((id, d) <- docs) yield (Index(idMap(id)) -> doc(d))
    init = CommunityMap(cs)
    return this
  }

  // A stream where each element is the clustering after another cycle of iterations
  lazy val cycleStream : Stream[CommunityMap] = init #:: cycleStream.map(_.cycle)
  lazy val cluster : CommunityMap = cycleStream.zip(cycleStream.tail).takeWhile({ case (m1,m2) => m1 != m2 }).toList.last._2

  implicit def linksToLinks(links : List[Link]) : Links = Links(links)
  case class Links(links : List[Link]) {

    // Take the n strongest connections
    def takeN(n : Int) : List[Link] = links.sortBy(_.weight).reverse.take(n)

    // Take only links over a certain treshold
    def overN(n : Int) : List[Link] = links.filter(_.weight > n)
  }
}


trait Community {
  // Variables that must be specified
  val cs : Map[Index, Community]
  val links : List[CommunityLink]

  // In which cluster is the community pointed to by a link
  val idMap : Map[Id, Index]

  // Lists of links leading out of the clique
  lazy val innerLinks = for (l <- links if idMap.contains(l.target)) yield l
  lazy val outerLinks = for (l <- links if !idMap.contains(l.target)) yield l

  // Sums of links
  lazy val K : Double = links.map(_.weight).sum

  // Used for calculating Q
  def getWeight(c : Community, l : CommunityLink) : Double = {
    val c1 = c.get(l.target)
    val c2 = c.get(l.source)
    return l.weight - c1.K * c2.K / (2.0 * K)
  }

  // The community score
  lazy val Q : Double = {
    1.0/K * (for ((i, c) <- cs; l <- c.innerLinks) yield getWeight(c, l)).sum
  }

  // Calculate the delta of adding c to this community
  def deltaQ(c : Community, m : Double) : Double = {
    val weight = for (l <- c.links if idMap.contains(l.target)) yield {
      l.weight - get(l.target).K * c.K / (2.0 * m)
    }
    2.0/m * weight.sum
  }

  // Calculate the delta of removing c from this community
  def deltaPrintQ(c : Community, m : Double) : Double = {
    val weight = for (l <- c.links if idMap.contains(l.target)) yield {
      //println("\tWeight:\t" + l.weight)
      //println("\tK * K:\t" + get(l.target).K * c.K)
      l.weight - get(l.target).K * c.K / (2.0 * m)
    }
    println(weight)
    2.0/m * weight.sum
  }

  // Add a community to this
  def add(c : Community, id : Id) : CommunityMap = CommunityMap(cs + (Index(id.n) -> c))
  
  // Remove a community from this
  def remove(id : Id) : Community = CommunityMap(cs - idMap(id))

  // Get child
  def get(id : Id) : Community = cs(idMap(id))

}


// Link
case class CommunityLink(source : Id, target : Id, weight : Int)


// An empty community
case object CommunityEmpty extends Community {
  val links : List[CommunityLink] = List.empty
  val idMap : Map[Id, Index] = Map.empty
  val cs : Map[Index, Community] = Map.empty
}


case class Id(n : Int)
case class Index(n : Int)


// A community consisting of a single document
case class CommunityDoc(id : Id, links : List[CommunityLink]) extends Community {
  val idMap : Map[Id, Index] = Map(id -> Index(id.n))
  val cs : Map[Index, Community] = Map(Index(id.n) -> this)
  override def remove(id : Id) : Community = {
    if (idMap.contains(id)) return CommunityEmpty
    else throw new Exception("can't remove id from communityDoc with id: " + id.n)
  }
}



// A community consisting of several communities
case class CommunityMap(cs : Map[Index, Community]) extends Community {
  val links : List[CommunityLink] = (for ((i, c) <- cs; l <- c.links) yield l).toList
  val idMap = for ((index, c) <- cs; (id, _) <- c.idMap) yield (id -> index)


  // Move a community 'a' to community where 'c' is present
  def move(a_id : Id, c_id : Id) : CommunityMap = {

    // fetch a, b and c
    val oldParent = get(a_id)
    val newParent = get(c_id)
    val a = oldParent.get(a_id)

    // Move community around
    val newB = oldParent.remove(a_id)
    val newC = newParent.add(a, a_id)

    // create new community map
    return CommunityMap(cs + (idMap(a_id) -> newB) + (idMap(c_id) -> newC))
  }


  // Find community 'n' and check if it should be added to any neighbors
  def iter(index : Int) : CommunityMap = {
    val id = Id(index)
    val parent = get(id)
    val c = parent.get(id)

    // The penalty we incur by removing c from its current cluster
    val penalty = parent.deltaQ(c, K)

    // List of gains from each of c's links in case we added c to their cluster
    //val qs = c.outerLinks.map(l => (l.target -> get(l.target).deltaQ(K, c))).toMap
    val oqs = c.outerLinks.map(l => move(id,l.target))
    val newQs = oqs.map(_.Q - Q)

    val qs = c.outerLinks.map(l => (l.target -> (get(l.target).deltaQ(c, K) - penalty)))

    //println(newQs.zip(qs.map(_._2)).map(q => q._1 - q._2).sum)

    val (maxId, maxVal) = qs.maxBy(_._2)

    // The id which corresponds to the maximum change
    //val (maxId, maxVal) = if (qs == Map.empty) (Id(0),-1.0) else qs.maxBy(_._2.Q)
    //val result = (this :: qs).maxBy(_.Q)

    // Move the document with the highest gain to the community if it's over 0
    val result = if (maxVal > 0.0000001) move(id, maxId) else this
    //println(maxVal - penalty)
    //println(result.Q - Q)
    if (result.Q < Q) throw new Exception("Q is lower in new iteration")
    return result
  }


  // Cycle through each element
  def cycle : CommunityMap = {
    return (0 to (cs.size - 1)).foldLeft(this)((a,n) => a.iter(n))
  }


  override def toString : String = {
    (for ((i, c) <- cs if c != CommunityEmpty) yield i.toString + ":\t" + c.idMap.keys.mkString(", ")).mkString("\n")
  }
}




