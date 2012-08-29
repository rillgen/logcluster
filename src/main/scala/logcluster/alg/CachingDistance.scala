package logcluster.alg

import grizzled.slf4j.Logging
import logcluster.util.logIfRelevant
import com.google.common.cache.CacheBuilder
import com.google.common.cache.Cache
import java.util.concurrent.Callable
import java.lang.management.ManagementFactory

class CachingDistance(val impl: (IndexedSeq[Any], IndexedSeq[Any], Double) => Double) extends Logging {

  val cacheSize = {
    val usagePerc = 0.35
    val estimatedEntrySize = 400
    val res = (ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getMax.toDouble * usagePerc / estimatedEntrySize).toInt
    logger.info("Selected cache size: %d" format res)
    res
  }
  
  val cache: Cache[(IndexedSeq[Any], IndexedSeq[Any]), java.lang.Double] = 
    CacheBuilder.newBuilder.maximumSize(cacheSize).recordStats.build()

  implicit def fn2Callable[A](f: () => A) = new Callable[A] { def call() = f() }
  
  def apply(a: IndexedSeq[Any], b: IndexedSeq[Any], stopDistance: Double): Double = {
    try cache.get((a, b), () => impl(a, b, stopDistance).asInstanceOf[java.lang.Double])
    finally logIfRelevant(cache.stats.requestCount().toInt)(c => logStats())
  }
  
  def logStats() { 
    val s = cache.stats()
    logger.info("Distance cache stats: totalAccesses: %d, hits: %d, ratio: %.3f, cache size: %d" 
      format (s.requestCount, s.hitCount(), s.hitRate, cache.size))
  }
  
}
