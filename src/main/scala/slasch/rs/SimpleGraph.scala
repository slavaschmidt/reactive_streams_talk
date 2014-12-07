package slasch.rs

import akka.actor.ActorContext
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._

import scala.concurrent.duration._
import akka.stream.scaladsl.FlowGraphImplicits._

/**
 * @author  slasch 
 * @since   30.11.2014.
 */
abstract class SimpleGraph {

  type Schedule
  type ScrapeResult
  type Validation
  type Price
  type Stock
  type Rule
  type SafetyCheck


  def now = System.currentTimeMillis()

  def build: Unit = {

    implicit val context: ActorContext = ???

    implicit val flowMaterializer = FlowMaterializer()

    def generateSchedules(time: Long): Stream[Schedule] = ???
    def scrape(schedule: Schedule): ScrapeResult = ???
    def logSite(s: ScrapeResult): Unit = ???
    def validate(s: ScrapeResult): Validation = ???
    def checkPrice(s: Validation): Price = ???
    def checkStock(s: Validation): Stock = ???
    def logRule(rule: SafetyCheck): Unit = ???
    def applyRule(i : (Price, Stock)): Rule = ???
    def checkSafety: (Rule) => SafetyCheck = ???


    val schedules   = Flow[Long] mapConcat generateSchedules
    val robots      = Flow[Schedule] map scrape
    val validations = Flow[ScrapeResult] map { site =>
      logSite(site)
      validate(site)
    }
    val pricing     = Flow[Validation] map checkPrice
    val stock       = Flow[Validation] map checkStock
    val ruler       = Flow[(Price, Stock)] map applyRule
    val safety      = Flow[Rule] map checkSafety

    val zip         = Zip[Price, Stock]
    val split       = Broadcast[Validation]

    val timer   = Source(0.seconds, 1.minute, () => now)

    val archive = ForeachSink[SafetyCheck] { logRule }

    val graph   = FlowGraph { implicit builder =>

      timer ~> schedules ~> robots ~> validations ~> split

      split ~> stock ~> zip.right

      split ~> pricing ~> zip.left ~> ruler ~> safety ~> archive

    }

    graph.run()


  }
}
