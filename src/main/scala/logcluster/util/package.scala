package logcluster

import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit
import java.io.File
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import scala.collection.JavaConversions._
import grizzled.slf4j.Logging
import java.io.IOException

package object util extends Logging {

  def measureExecTime[A](printer: Long => Unit)(toMeasure: => A) = {
    val start = System.currentTimeMillis
    try toMeasure
    finally printer(System.currentTimeMillis - start) 
  }
  
  def getExecTime(toMeasure: => Unit): Long = {
    val start = System.currentTimeMillis
    toMeasure
    System.currentTimeMillis - start 
  }
  
  /**
   * A Traversable over a blocking queue that blocks in each read, except the queue has been marked as finished 
   * (in which case ends).
   */
  class BlockingQueueTraversable[A](queue: BlockingQueue[A], finished: AtomicBoolean) extends Traversable[A] {
    def foreach[U](f: (A) => U) {
      while (!finished.get || !queue.isEmpty) {
        val elem = queue.poll(100, TimeUnit.MILLISECONDS)
        if (elem != null)
          f(elem)
      }
    }
  }
  
  def createDirOrCheckEmpty(dir: File) {
    dir.mkdirs()
    val list = dir.list
    if (list == null)
      throw new IOException("Cannot create or access directory %s" format dir);
    if (list.length > 0)
      throw new IOException("The directory %s is not empty!" format dir);
  }
  
  def newThread(threadName: String)(thunk: => Unit) = {
    val runnable = new Runnable { 
      def run() = {
        try thunk
        catch { case e: Throwable => logger.error("Error in %s" format threadName, e) }
      }
    }
    new Thread(runnable, threadName)
  }
  
  def using[A <: { def close() }, B](resource: A)(block: A => B) = {
    try block(resource)
    finally if (resource != null) resource.close()
  }
  
  def logIfRelevant(c: Int)(f: Int => Unit) = {
    if (c < 100 && c % 10 == 0 || 
        c < 1000 && c % 100 == 0 || 
        c < 10000 && c % 1000 == 0 || 
        c < 100000 && c % 10000 == 0 || 
        c < 1000000 && c % 100000 == 0 ||
        c < 10000000 && c % 1000000 == 0)
      f(c)
  }

}
