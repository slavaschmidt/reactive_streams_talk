package slasch.rs

import akka.actor._
import akka.io.IO
import spray.can.Http

object Boot extends App {

  def initActors(implicit system: ActorSystem) = {
    system.actorOf(Props[HeartBeatActor], "mpc-service")
  }

  def startServer(service: ActorRef)(implicit system: ActorSystem) {
    IO(Http) ! Http.Bind(service, interface = "localhost", port = 7070)
  }

  def init() {
    implicit val system = ActorSystem("TestCenter")
    startServer(initActors)
  }

  init()

}
