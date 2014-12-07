package slasch.rs

import java.nio.ByteBuffer
import java.nio.channels.Pipe

import slasch.rs.Env._
/**
 * @author  slasch 
 * @since   18.11.2014.
 */
object NIORemote {

  def create(rateA: Int, rateB: Int) = {
    val pipe = Pipe.open()
    val sinkChannel = pipe.sink
    val sourceChannel = pipe.source
    sourceChannel.configureBlocking(false)
    sinkChannel.configureBlocking(false)

    val borice = new SThread("RS-Borice") {
      override def swap(n: Int) {
        val buffer = ByteBuffer.allocate(n * rateB)
        val cnt = sourceChannel.read(buffer)
        super.swap(cnt)
      }
    }

    val alice = new SThread("RS-Alice") {
      override def swap(n: Int) {
        val cnt = n * rateA
        val buffer = ByteBuffer.allocate(cnt)
        buffer.put(Vector.fill(cnt)(SCRUPT).toArray)
        buffer.flip()
        val written = sinkChannel.write(buffer)
        super.swap(written)
      }
    }

    akka.japi.Pair(alice, borice)
  }
}