package slasch.rs

import java.io.{PipedInputStream, PipedOutputStream}

/**
 * @author  slasch 
 * @since   24.11.2014.
 */
object IORemote {

  def create(size:Int, rateA: Int, rateB: Int) = {
    val out = new PipedOutputStream()
    val in  = new PipedInputStream(out, size)

    val borice = new SThread("RS-Borice") {
      override def swap(n: Int) {
        super.swap(n)
        for (i <- 0 to n * rateB) in.read()
      }
    }

    val alice = new SThread("RS-Alice") {
      override def swap(n: Int) {
        super.swap(n)
        for (i <- 0 to n * rateA) out.write(Env.SCRUPT)
      }
    }

    akka.japi.Pair(alice, borice)
  }

}