package slasch.rs

import akka.actor.Actor

import scala.annotation.tailrec
import scala.util.Random
/**
 * @author  slasch
 * @since   05.11.2014.
 */
import slasch.rs.Env._

abstract class SThread(name: String) extends Thread(name) {
  self: Thread =>
  def swap(n: Int = 1) {
    Measurer.currentSwaps(name).addAndGet(n)
  }
  var stopped: Boolean = false
  def stopRunning() = { this.stopped = true }
  override def run(): Unit = while (!stopped) {
    work()
    swap(scrupts)
  }
}


class LocalTest extends Actor {

  // can be one of:

  // J6Local.create
  // IORemote.create(3,1,1)
  // NIORemote.create(3,1)
  // J8Stream.create
  // ReactiveStreams.create(context.system)

  val akka.japi.Pair(alice, borice) = ReactiveStreams.create(context.system)

  override def receive: Receive = {
    case Env.Start =>
      alice.start()
      borice.start()
    case Env.Stop =>
      alice.stopRunning()
      borice.stopRunning()

  }
}

object Env {
  case object Start
  case object Stop

  val SCRUPT: Byte = 0xFF.toByte

  val maxWork = 20000000
  val maxScrupts = 10

  def workTime  = maxWork     / 2 + Random.nextInt(maxWork / 2)
  def scrupts   = maxScrupts  / 2 + Random.nextInt(maxScrupts / 2)

  @tailrec
  def trig(count: Int, input: Double):Double =
    if (count<=0) math.tan(math.atan(input))
    else trig(count-1, math.tan(math.atan(input)))

  def work() = trig(workTime, 0.5)
}
