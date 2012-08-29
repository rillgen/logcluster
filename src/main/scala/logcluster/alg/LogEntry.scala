package logcluster.alg

import java.util.regex.Pattern

case class LogEntry(original: String, msg: String, tokens: IndexedSeq[String])

object LogEntry {
  
  val pattern = Pattern.compile("""[ ,/\\=\[\]():{};"'?&]+""")
  
  def apply(original: String, msg: String) =
    new LogEntry(original, msg, pattern.split(msg).filterNot(_.isEmpty).map(_.intern))
  
}