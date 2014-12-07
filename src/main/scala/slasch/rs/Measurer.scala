package slasch.rs

import java.lang.management.{ManagementFactory, ThreadInfo, ThreadMXBean}
import java.util.concurrent.atomic.AtomicLong

/**
 * @author  slasch
 * @since   05.11.2014.
 */
object Measurer {

  private val lastValues    : scala.collection.mutable.Map[String, Measurement] = scala.collection.mutable.Map.empty
  private val lastSwaps     : scala.collection.mutable.Map[String, Long]         = scala.collection.mutable.Map.empty

  val currentSwaps  : scala.collection.mutable.Map[String, AtomicLong]   =
    scala.collection.mutable.Map("RS-Alice" -> new AtomicLong(0), "RS-Borice" -> new AtomicLong(0))

  case class Measurement(name: String, time: Long, systemNanos: Long, userNanos: Long, state: String) {
    def this(thread: ThreadInfo) = this(thread.getThreadName, System.currentTimeMillis(),
      mb.getThreadCpuTime(thread.getThreadId), mb.getThreadUserTime(thread.getThreadId), thread.getThreadState.name)
  }
  case class ChartResult(name: String, time: Long, cpu: Double, swap: Double, state: String) {
    def this(thread: ThreadInfo, cpu: Double, swap: Double) =
      this(thread.getThreadName, System.currentTimeMillis(), cpu, swap, thread.getThreadState.name)
  }

  private val mb = ManagementFactory.getPlatformMXBeans(classOf[ThreadMXBean]).get(0)
  private val filterName = "RS"
  private def filterNames: (ThreadInfo) => Boolean = _.getThreadName startsWith filterName

  def get = {
    val threads = mb.dumpAllThreads(true, true) filter filterNames
    val result = for {
      thread    <- threads
      name      = thread.getThreadName
      swapNow   = currentSwaps(name).get()
      last      <- lastValues.put(name, new Measurement(thread))
      lastSwap  <- lastSwaps.put(name, swapNow)
      valDiff   = mb.getThreadUserTime(thread.getThreadId) - last.userNanos
      timeDiff  = System.currentTimeMillis() - last.time
      swapDiff  = swapNow - lastSwap
      cpu       = valDiff.toDouble / timeDiff.toDouble / 1000d
      swap      = swapDiff.toDouble / timeDiff.toDouble * 5000d
    } yield {
      new ChartResult(thread, cpu, swap)
    }
    result.toVector
  }

}
