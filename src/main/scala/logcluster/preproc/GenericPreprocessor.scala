package logcluster.preproc
import logcluster.alg.LogEntry

object GenericPreprocessor extends Preprocessor {
  
  val regex = "[ ;:]E(RROR|rror)[ ;:]".r
  def apply(line: String) = regex.findFirstMatchIn(line).map(m => LogEntry(original = line, msg = line))
  
}
