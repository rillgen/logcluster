package logcluster.alg

import com.typesafe.scalalogging.StrictLogging

class Cluster(val id: String, val example: String, val fundadoid: IndexedSeq[String], val minSimilarity: Double) {

  var entryCount = 0

  def calcSimil(entry: LogEntry) = Cluster.cachedLevenshteinDistance(entry.tokens, fundadoid, minSimilarity)

  def prettyPrint = example

  def addEntry(entry: LogEntry) {
    entryCount += 1
  }

}

object Cluster extends StrictLogging {

  val cachedLevenshteinDistance = new CachingDistance(Levenshtein.apply)

  def apply(entry: LogEntry, number: Int, minSimilarity: Double) =
    new Cluster(id = ("cluster-%d" format number), entry.msg, entry.tokens, minSimilarity)

}
