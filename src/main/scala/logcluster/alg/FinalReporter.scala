package logcluster.alg

import java.io.File
import java.io.PrintWriter
import logcluster.util.using
import scala.io.Source
import java.io.FileWriter

object FinalReporter {
  
  val style = 
    "body, table    { font-size: 10pt } " +
    "p              { margin-top: 0; margin-bottom: 0 } " +
    ".example       { word-wrap: break-word; font-size: 8pt; font-family: monospace } " +
    ".odd           { background-color: rgb(245,245,245) } " +
    "h1             { font-size: 14pt } " +
    "table          { margin-top: 13px; margin-bottom: 10px } " +
    "table td       { padding: 2px 4px } " +
    "table thead td { font-weight: bold } "

  def produceReport(title: String, dir: File) {
    val clusters = dir.listFiles().toSeq.filter(_.getName.startsWith("cluster-"))
    val clustersSize = clusters.map(c => (c, countMembers(c))).toMap
    val errorCount = clustersSize.values.sum
    val medianSize = if (clusters.size > 0) Some(median(clusters.map(clustersSize).toIndexedSeq)) else None
    val file = new PrintWriter(new FileWriter(new File(dir, "index.html")))
    file.write("<html>")
    file.write("<head><style>%s</style></head>" format style)
    file.write("<body>")
    file.write("<h1>%s</h1>" format title)
    file.write("<p>Total entry count: %d</p>" format errorCount)
    file.write("<p>Grouped <strong>%d</strong> errors in %d clusters</p>" format (errorCount, clusters.size))
    medianSize.foreach(s => file.write("<p>Median cluster size: %.1f</p>" format s))
    file.write("<table>")
    file.write("""<thead><tr class="odd"><td>Entries</td><td>Name</td><td>Archetype</td></tr></thead>""")
    file.write("<tbody>")
    for ((cluster, index) <- clusters.sortBy(c => -clustersSize(c)).zipWithIndex) {
      val odd = index % 2 == 1
      val rowClass = if (odd) "odd" else ""
      val id = cluster.getName
      val example = escape(readFirst(cluster).take(350))
      val size = clustersSize(cluster)
      file.write(s"""<tr class="$rowClass"><td>$size</td><td><a href="$id">$id</a></td><td class="example">${example}...</td></tr>""")
    }
    file.write("</tbody>")
    file.write("</table>")
    file.write("""<div class="footer">Powered by <a href="https://github.com/despegar/logcluster">logcluster</a></div>""")
    file.write("</body>")
    file.write("</html>")
    file.close()
  }
  
  def countMembers(clusterFile: File) = using(Source.fromFile(clusterFile))(_.getLines.size)
  
  def readFirst(clusterFile: File) = using(Source.fromFile(clusterFile))(_.getLines.next)
  
  def escape(str: String) = str.replaceAll("&", "&amp;").replaceAll("<", "&lt;")

  /*
   * Naïve implementation
   */
  def median(s: IndexedSeq[Int]) = {
    val (lower, upper) = s.sorted.splitAt(s.size / 2)
    if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
  }
  
}