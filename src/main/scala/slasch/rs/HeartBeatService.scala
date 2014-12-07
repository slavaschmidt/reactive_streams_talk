package slasch.rs

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Props}
import spray.can.Http
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.{HttpService, RequestContext}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration

trait HeartBeatService extends HttpService with DefaultJsonProtocol with SprayJsonSupport {

  self: Actor =>

  implicit def executionContext: ExecutionContextExecutor = context.dispatcher

  val time = FiniteDuration(100, TimeUnit.MILLISECONDS)

  val heartbeatRoute = {
    get {
      pathPrefix("assets") {
        getFromResourceDirectory("www/")
      } ~
      path("favicon.ico") {
        getFromResource("www/favicon.ico")
      } ~
      path("index.html") {
        getFromResource("www/index.html")
      } ~
      path("/") {
        getFromResource("www/index.html")
      } ~
      path("heartbeat") {
        sendStreamingResponse
      } ~
      path("start") {
        complete {
          startWorkers()
          "OK"
        }
      }
    }
  }

  // we prepend 2048 "empty" bytes to push the browser to immediately start displaying the incoming chunks
  lazy val streamStart = "\r\n\r\n" // " " * 2048
  lazy val streamEnd = "\r\n\r\n"

  implicit val ResultFormat = jsonFormat5(Measurer.ChartResult)

  case class Okey(remaining: Int)

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    context.system.scheduler.scheduleOnce(duration)(body)

  def startWorkers() = {
    context.system.actorSelection("/user/tester-actor") ! Env.Start
  }

  def sendStreamingResponse(ctx: RequestContext): Unit =
    actorRefFactory.actorOf {
      Props {
        new Actor with ActorLogging {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(ContentType(MediaType.custom("text/event-stream")), streamStart))
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Okey(1600))

          def receive = {
            case Okey(0) =>
              ctx.responder ! MessageChunk(streamEnd)
              ctx.responder ! ChunkedMessageEnd
              context.stop(self)

            case Okey(remaining) =>
              in(time) {
                val info = Measurer.get
                val json = info map ResultFormat.write map { _.compactPrint }
                if (json.nonEmpty) {
                  val jsonStr = json.mkString("[", ",", "]")
                  val nextChunk = MessageChunk(s"""data: { "data": $jsonStr } $streamEnd""")
                  ctx.responder ! nextChunk.withAck(Okey(remaining - 1))
                } else {
                  self ! Okey(remaining)
                }
              }

            case ev: Http.ConnectionClosed =>
              log.info("Stopping response streaming due to {}", ev)
          }
        }
      }
    }

}