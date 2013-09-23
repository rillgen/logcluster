package logcluster.alg

import java.io.File
import java.io.PrintStream
import java.io.FileOutputStream
import logcluster.util.createDirOrCheckEmpty
import com.typesafe.scalalogging.slf4j.Logging

class Reporter(val title: String, val dir: File) extends Logging {

  createDirOrCheckEmpty(dir)
  logger.info("Saving clusters in directory " + dir)

  val clusters = new scala.collection.mutable.HashMap[String, (Cluster, PrintStream)]

  var totalEntryCount = 0L
  
  def newCluster(cluster: Cluster) {
    val file =  new PrintStream(new FileOutputStream(new File(dir, cluster.id)))
    clusters += cluster.id -> (cluster, file)
  }

  def addToCluster(cluster: Cluster, entry: String) {
    val (_, stream) = clusters(cluster.id)
    stream.println(entry)
    stream.flush()
  }

  def close() = {
    for ((_, stream) <- clusters.values)
      stream.close()
  }
  
}
