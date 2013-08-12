package logcluster.preproc

import logcluster.alg.LogEntry

object GenericSensitivePreprocessor extends Preprocessor {

  val regex = "[ ;:]ERROR[ ;:]".r
  def apply(line: String) = regex.findFirstMatchIn(line).map(m => LogEntry(original = line, msg = line))

}