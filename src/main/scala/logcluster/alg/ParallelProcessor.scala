package logcluster.alg

import logcluster.preproc.Preprocessor
import logcluster.util.getExecTime
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import logcluster.util.BlockingQueueTraversable
import logcluster.util.newThread
import logcluster.util.logIfRelevant
import java.util.concurrent.atomic.AtomicInteger
import com.typesafe.scalalogging.slf4j.Logging
import java.util.concurrent.BlockingQueue
import scala.collection.mutable

/**
 * A parallel processor that does the filtering (many log entries, I/O bound) in a different thread than
 * the clustering (a few log entries, CPU bound) so both tasks can advance simultaneously without waiting
 * each other for every line
 */
class ParallelProcessor(
    val logFile: String, 
    val preproc: Preprocessor, 
    val minSimil: Double, 
    val reporter: Reporter,
    initialClusterList: Map[String, Cluster] = Map[String, Cluster]()
  ) extends Logging {

  val (actualComp, potentialComp) = (new AtomicInteger, new AtomicInteger)

  val clusterList = mutable.Map(initialClusterList.toSeq: _*)
  
  // These work as a protection against very long log entries. 
  val maxLogSize = 2500
  def trimLog(line: String) = line.substring(0, math.min(maxLogSize, line.size))
  
  def doIt(lines: Iterator[String]) {
    logger.info("Starting clustering using preprocessor %s and minimum similarity %.2f" format
      (preproc.getClass.getSimpleName, minSimil))
    val buffer = new LinkedBlockingQueue[LogEntry](1000)
    val finished = new AtomicBoolean
    val producer = newThread(s"reader-$logFile") {
      try {
        var i = 0L
        for (line <- lines.map(l => preproc(trimLog(l)))) {
          logIfRelevant(i + 1)(c => logger.debug(s"Processed $c lines"))
          line.foreach(buffer.put(_))
        }
        logger.info(s"Preprocessing finished. Total entry count: $i")
        reporter.totalEntryCount = i
      } finally finished.set(true)
    }
    producer.setDaemon(true) // So the producer won't be blocked forever if the consumer threw an exception
    producer.start()
    doClustering(new BlockingQueueTraversable(buffer, finished))
  }
  
  def doClusteringAsync(errors: Traversable[LogEntry]) {
    val consumer = newThread(s"reader-$logFile") {
      doClustering(errors)
    }
    consumer.setDaemon(true)
    consumer.start()
  }
  
  def doClustering(errors: Traversable[LogEntry]) {
    val time = getExecTime {
      var errorCount = 0L
      for (line <- errors) {
        classifyEntry(line)
        errorCount += 1
        logIfRelevant(errorCount)(c => logger.debug(s"Processed $c errors"))
      }
      logger.info("Clustering finished: %d errors and %d clusters." format (errorCount, clusterList.size))
      logger.info("Did %d comparisions between entries and clusters (out of %d possible)" format
        (actualComp.get, potentialComp.get))
    }
    logger.info("Total time: %d seconds" format (time / 1000))
  }

  def classifyEntry(entry: LogEntry) {
    findNearestCluster(entry) match {
      case Some(cluster) =>
        addToCluster(cluster, entry)
      case None => {
        val cluster = Cluster(entry, clusterList.size, minSimil)
        clusterList += cluster.id -> cluster
        reporter.newCluster(cluster.id)
        addToCluster(cluster, entry)
        logIfRelevant(clusterList.size)(c => logger.debug("Found %d clusters" format c))
      }
    }
  }

  def addToCluster(cluster: Cluster, entry: LogEntry) {
    cluster.addEntry(entry)
    reporter.addToCluster(cluster.id, entry.original)
  }

  /*
   * Obtiene (en paralelo) el cluster mas cercano (si alguno lo es suficientemente)
   */
  def findNearestCluster(entry: LogEntry): Option[Cluster] = {
    val shouldStop = new AtomicBoolean
    val distancies = for (cluster <- clusterList.values.par) yield {
      potentialComp.incrementAndGet
      if (shouldStop.get) {
        // Si algun cluster dio con más de la similitud minina, no se sigue buscando
        (cluster, 0.0)
      } else {
        actualComp.incrementAndGet
        val simil = cluster.calcSimil(entry)
        if (simil > minSimil)
          shouldStop.set(true)
        (cluster, simil)
      }
    }
    // Igual puede haber más de uno por concurrencia, se toma el mejor
    val optionalBest = distancies.reduceOption { (previous, it) =>
      val (_, currSimil) = it
      val (_, previousSimilarity) = previous
      if (currSimil > previousSimilarity)
        it
      else
        previous
    }
    optionalBest.flatMap {
      case (bestCluster, bestSimil) =>
        if (bestSimil > minSimil) Some(bestCluster) else None
    }
  }

}