package logcluster.alg

import java.lang.management.ManagementFactory

import com.github.blemale.scaffeine.Scaffeine
import com.typesafe.scalalogging.StrictLogging
import logcluster.util.logIfRelevant


class CachingDistance(val impl: (IndexedSeq[Any], IndexedSeq[Any], Double) => Double) extends StrictLogging {

  val cacheSize = {
    val usagePerc = 0.35
    val estimatedEntrySize = 400
    val res = (ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getMax.toDouble * usagePerc / estimatedEntrySize).toInt
    logger.info("Selected cache size: %d" format res)
    res
  }

  val cache =
    Scaffeine()
      .recordStats()
      .maximumSize(cacheSize)
      .build[(IndexedSeq[Any], IndexedSeq[Any]), Double]()

  import scala.language.implicitConversions

  def apply(a: IndexedSeq[Any], b: IndexedSeq[Any], stopDistance: Double): Double = /*sync.caching((a, b))(None)*/ {
    try cache.get((a, b), _ => impl(a, b, stopDistance).asInstanceOf[java.lang.Double])
    finally logIfRelevant(cache.stats.requestCount)(c => logStats())
  }

  def logStats() {
    val s = cache.stats()
    logger.info("Distance cache stats: totalAccesses: %d, hits: %d, ratio: %.3f, cache size: %d"
      format(s.requestCount, s.hitCount, s.hitRate, cache.estimatedSize))
  }

}