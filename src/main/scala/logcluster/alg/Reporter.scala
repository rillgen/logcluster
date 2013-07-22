package logcluster.alg

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.PrintStream
import java.io.FileOutputStream
import logcluster.util.createDirOrCheckEmpty
import com.typesafe.scalalogging.slf4j.Logging

class Reporter(title: String, dir: File) extends Logging {

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

  val style = 
    "body, table    { font-size: 10pt } " +
    "p              { margin-top: 0; margin-bottom: 0 } " +
    ".example       { word-wrap: break-word; font-size: 8pt; font-family: monospace } " +
    ".odd           { background-color: rgb(245,245,245) } " +
    "h1             { font-size: 14pt } " +
    "table          { margin-top: 13px; margin-bottom: 10px } " +
    "table td       { padding: 2px 4px } " +
    "table thead td { font-weight: bold } "

  def produceReport(totalTime: Long) {
    val clusterList = (clusters.values.map { case (cluster, _) => cluster }).toIndexedSeq
    val errorCount = clusterList.map(_.entryCount).sum
    val medianSize = if (clusters.size > 0) Some(median(clusterList.map(_.entryCount))) else None
    val file = new PrintWriter(new FileWriter(new File(dir, "index.html")))
    file.write("<html>")
    file.write("<head><style>%s</style></head>" format style)
    file.write("<body>")
    file.write("<h1>%s</h1>" format title)
    file.write("<p>Total entry count: %d</p>" format totalEntryCount)
    file.write("<p>Grouped <strong>%d</strong> errors in %d clusters</p>" format (errorCount, clusters.size))
    medianSize.foreach(s => file.write("<p>Median cluster size: %.1f</p>" format s))
    file.write("<p>Total time: %d seconds</p>" format (totalTime / 1000))
    file.write("<table>")
    file.write("""<thead><tr class="odd"><td>Entries</td><td>Name</td><td>Archetype</td></tr></thead>""")
    file.write("<tbody>")
    for ((cluster, index) <- clusterList.sortBy(-_.entryCount).zipWithIndex) {
      val odd = index % 2 == 1
      val rowClass = if (odd) "odd" else ""
      file.write("""<tr class="%s"><td>%d</td><td><a href="%s">%s</a></td><td class="example">%s...</td></tr>"""
          format (rowClass, cluster.entryCount, cluster.id, cluster.id, escape(cluster.prettyPrint.take(350))))
    }
    file.write("</tbody>")
    file.write("</table>")
    file.write("""<div class="footer">Powered by <a href="https://github.com/despegar/logcluster">logcluster</a></div>""")
    file.write("</body>")
    file.write("</html>")
    file.close()
  }
  
  def escape(str: String) = str.replaceAll("&", "&amp;").replaceAll("<", "&lt;")

  /*
   * Naïve implementation
   */
  def median(s: IndexedSeq[Int]) = {
    val (lower, upper) = s.sorted.splitAt(s.size / 2)
    if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
  }
  
  def close() = {
    for ((_, stream) <- clusters.values)
      stream.close()
  }
  
}
