package paper

// Object for transforming a list of documents to a communityMap
object Louvain {

  var idMap : Map[String, Int] = Map.empty
  var indexMap : Map[Int, String] = Map.empty

  def init[A <: DataItem](docs : Map[String, Document[A]]) : Louvain = {
    idMap = for (((id, _), i) <- docs.zipWithIndex) yield (id -> i)
    indexMap = for (((id, _), i) <- docs.zipWithIndex) yield (i -> id)
    def link(id : String, l : Link) = CommunityLink(Id(idMap(id)), Id(idMap(l.id)), l.weight)
    def doc(d : Document[A]) = CommunityDoc(Id(idMap(d.id)), d.links.map(link(d.id,_)))
    val cs : Map[Index, CommunityDoc] = for ((id, d) <- docs) yield (Index(idMap(id)) -> doc(d))
    Louvain(CommunityMap(cs))
  }

  def cluster(l : Louvain) : Map[String, Int] = {
    for (((_,c),i) <- l.cluster.cs.zipWithIndex; (_,d) <- c.cs) yield (indexMap(d.cs.keys.head.n) -> i)
  }

}


// Case class for clustering
case class Louvain(community : CommunityMap) {

  // A stream where each element is the clustering after another cycle of iterations
  lazy val cycleStream : Stream[CommunityMap] = community #:: cycleStream.map(_.cycle)
  lazy val convergentStream : Stream[CommunityMap] = (cycleStream.zip(cycleStream.tail)
                                                      .takeWhile { case (m1,m2) => m1 != m2 }
                                                      .map { case (s1,s2) => s2 })
  lazy val qs : Stream[Double] = cycleStream.map { c => c.Q }
  lazy val cluster : CommunityMap = convergentStream.last

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

  // Add a community to this
  def add(c : Community, id : Id) : CommunityMap = CommunityMap(cs + (Index(id.n) -> c))
  
  // Remove a community from this
  def remove(id : Id) : Community = CommunityMap(cs - idMap(id))

  // Get child
  def get(id : Id) : Community = cs(idMap(id))

}


// Link
case class CommunityLink(source : Id, target : Id, weight : Int)


case class Id(n : Int)
case class Index(n : Int)


// A community consisting of a single document
case class CommunityDoc(id : Id, links : List[CommunityLink]) extends Community {
  val idMap : Map[Id, Index] = Map(id -> Index(id.n))
  val cs : Map[Index, Community] = Map(Index(id.n) -> this)
  override def remove(id : Id) : Community = {
    if (idMap.contains(id)) return CommunityMap(Map.empty)
    else throw new Exception("can't remove id from communityDoc with id: " + id.n)
  }
}



// A community consisting of several communities
case class CommunityMap(cs : Map[Index, Community]) extends Community {
  val links : List[CommunityLink] = (for ((i, c) <- cs; l <- c.links) yield l).toList
  val idMap = for ((index, c) <- cs; (id, _) <- c.idMap) yield (id -> index)

  // Move a community 'a' to community where 'c' is present
  def move(a_id : Id, c_id : Id) : CommunityMap = {

    // fetch a and c
    val oldParent = get(a_id)
    val newParent = get(c_id)
    val a = oldParent.get(a_id)

    // Check if a is in c
    if (oldParent == newParent) return this

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
    val qs = c.outerLinks.map(l => (l.target -> (get(l.target).deltaQ(c, K) - penalty)))

    // Getting maximal value
    val (maxId, maxVal) = ((id -> 0.0) :: qs).maxBy(_._2)

    // Move the document with the highest gain to the community if it's over 0
    val result = move(id, maxId)

    // Guarantee that if there's an error in here somewhere, we will fail
    if (result.Q < Q) throw new Exception("Q is lower in new iteration")
    return result
  }


  // Cycle through each element
  def cycle : CommunityMap = {
    return (0 to (cs.size - 1)).foldLeft(this)((a,n) => a.iter(n))
  }


  override def toString : String = {
    (for ((i, c) <- cs if c.cs.size > 0) yield i.toString + ":\t" + c.idMap.keys.mkString(", ")).mkString("\n")
  }


  // Reindex a particular community in case we want to cluster it further
  def rebase : CommunityMap = {
    val map = for (((i, c), index) <- cs.zipWithIndex) yield (i, Index(index))
    val c = CommunityMap(for ((i, c) <- cs if (c.cs.size > 0)) yield (i -> c))
    c.rebase(map)
  }

  def rebase(indexMap : Map[Index, Index]) : CommunityMap = {
    CommunityMap(for ((i, c) <- cs) yield c match {
      case (c : CommunityDoc) => (indexMap(i) -> c)
      case (c : CommunityMap) => (indexMap(i) -> c.rebase(indexMap))
    })
  }

}




