package slasch.rs

import java.awt.Color

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

/**
 * @author  slasch 
 * @since   30.11.2014.
 */

import javax.swing.JFrame

object TestUI extends App {
  val window = new JFrame()
  window.getContentPane().setBackground(Color.decode("#0A2939"))
  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  window.setBounds(0, 0, 500, 500)
  window.setVisible(true)
  new FunctionStreamTest().start
}

class FunctionStreamTest {

  type Input = List[Int]
  implicit val system = ActorSystem("FunctionalTest")
  implicit val mat = FlowMaterializer()
  implicit val context = ExecutionContext.global

  def setColor(i: List[Int]) = {
    val g = TestUI.window.getGraphics
    g.setColor(Color.getHSBColor(i(0), i(1), i(2)))
    g
  }

  val display = {
    def ellipse (i: Input) = setColor(i).fillOval(i(3), i(4), i(5), i(6))
    def rect    (i: Input) = setColor(i).fillRect(i(3), i(4), i(5), i(6))

    val logics: Seq[(Input) => Unit] = Seq(ellipse, rect)
    def rnd = Random.nextInt(255)

    val timer       = Source(0.seconds, 1.second, () => rnd )
    val randoms     = Source { () => Some(rnd) }

    val functions   = timer map { i => logics(i % logics.size) }

    val display     = functions map { f =>
      val groups = randoms.take(7)
      val params = groups.fold(List.empty[Int])((l, i) => i :: l)

      for { p <- params } f(p)
    }

    display
  }

  def start = {
    display.runWith(BlackholeSink)
  }
}


