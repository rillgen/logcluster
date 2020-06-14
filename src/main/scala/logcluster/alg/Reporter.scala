package logcluster.alg

import java.io.{File, FileOutputStream, IOException, PrintStream}

import com.typesafe.scalalogging.StrictLogging
import logcluster.util.createDirOrCheckEmpty

import scala.collection.mutable

class Reporter(val title: String, val dir: File, append: Boolean = false) extends StrictLogging {

  if (append)
    Reporter.createDirIfNecessary(dir)
  else
    createDirOrCheckEmpty(dir)

  logger.info("Saving clusters in directory " + dir)

  val clusters = mutable.HashMap[String, PrintStream]()

  var totalEntryCount = 0L

  def newCluster(clusterId: String) {
    // Do nothing, file is created in the first addition
  }

  def addToCluster(clusterId: String, entry: String) {
    val stream = clusters.get(clusterId) match {
      case Some(existing) => existing
      case None =>
        val newStream = new PrintStream(new FileOutputStream(new File(dir, clusterId), append))
        clusters += clusterId -> newStream
        newStream
    }
    stream.println(entry)
    stream.flush()
  }

  def close() = {
    for (stream <- clusters.values)
      stream.close()
  }

}

object Reporter {

  def createDirIfNecessary(dir: File) {
    dir.mkdirs()
    val list = dir.list
    if (list == null)
      throw new IOException("Cannot create or access directory %s" format dir);
  }

}