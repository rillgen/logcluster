Logcluster is a simple clustering implementation that is targeted specifically to the task of grouping errors in log files. Usually, log file entries (and thus errors) are produced from few templates. Once written in a file, it can be useful to identify and group the entries that originated from the same template. In the case of errors, the same template means the same point of origin and thus likely the same fix.

As this algorithm is meant to be run on big log files, it is explicitly designed to have linear complexity respect to the number of entries. There is no known general clustering algorithm with this complexity and this is not the first one. The approach followed is lousy based in the k-means algorithm, but has important simplifications that make the result undoubtedly worse, but surprisingly acceptable in practice.

The same log template can usually be instantiated using parameters of different lengths. This fact can destroy the effectiveness of a positional euclidean distance. Levenshtein distance is used instead. Log entries are first tokenized in order to reduce the number of “characters”. One tokenized, there is only one pass through the lines. Each line is compared to a “representative” of each existing cluster. If the new line is similar enough to one of them, it is added to that cluster. If none of the clusters has more than the (parameterized) minimal similarity, a new cluster is created and the line is its representative.

The algorithm is dependant on the order of the lines (once the cluster is created, its representative is never changed), but in the typical application it does not appear to a significant issue.

The implementation is also heavily parallel: will use as many cores it can. It has been tested with error sets in the order of the few millions, running in that case in less than four hours in a 12-core machine.

