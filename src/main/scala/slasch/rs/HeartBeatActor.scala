package slasch.rs

import akka.actor.Actor
import org.reactivestreams.Subscription

class HeartBeatActor extends Actor with HeartBeatService {

  def actorRefFactory = context

  def receive = runRoute(heartbeatRoute)

}
