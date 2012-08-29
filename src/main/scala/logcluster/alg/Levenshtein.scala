package logcluster.alg

import scala.math._

object Levenshtein {
  
  final class RealMatrix(val height: Int, val width: Int) {
    val v = Array.ofDim[Int](height * width)
    def apply(a: Int, b: Int) = v(a * width + b)
    def update(a: Int, b: Int, n: Int) { v(a * width + b) = n }
  }
  
  def unormalizeMinSimilarity(similarity: Double, maxLen: Int) = maxLen - maxLen * similarity

  def calc(i: Int, j: Int, tokens1: IndexedSeq[Any], tokens2: IndexedSeq[Any], d: RealMatrix) = {
	  if (i <= tokens1.length && j <= tokens2.length) {
	    val cost = if (tokens1(i-1) == tokens2(j-1)) 0 else 1
	    val v1 = d(i-1,j  ) + 1
	    val v2 = d(i  ,j-1) + 1
	    val v3 = d(i-1,j-1) + cost
	    d(i,j) = min(min(v1, v2), v3)
	    d(i,j)
	  } else { 
	    Int.MaxValue
	  }
  }
  
  def apply(a: IndexedSeq[Any], b: IndexedSeq[Any], minSimilarity: Double): Double = {
    val len1 = a.length
    val len2 = b.length
    val stopDistance = unormalizeMinSimilarity(minSimilarity, max(len1, len2))
    var qty = 0
    val d = new RealMatrix(len1 + 1, len2 + 1)
    for (i <- 0 to len1) d(i,0) = i
    for (j <- 0 to len2) d(0,j) = j
    for (i <- 1 to max(len1, len2)) {
      var minDist = Int.MaxValue
      for (j <- 1 until i) {
        minDist = min(minDist, calc(i,j,a,b,d))
        minDist = min(minDist, calc(j,i,a,b,d))
        qty += 2
      }
      minDist = math.min(minDist, calc(i,i,a,b,d))
      qty += 1
      if (minDist > stopDistance) 
        return 0
    }
    val nnld = d(len1,len2)
    val maxLength = max(len1, len2)
    (maxLength - nnld).toDouble / maxLength
  }
  
}