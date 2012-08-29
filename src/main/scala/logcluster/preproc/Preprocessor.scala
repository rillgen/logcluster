package logcluster.preproc
import logcluster.alg.LogEntry

trait Preprocessor {
  def apply(line: String): Option[LogEntry]
}