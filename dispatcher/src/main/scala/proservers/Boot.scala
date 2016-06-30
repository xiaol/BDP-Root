package proservers

import akka.actor.ActorSystem

import scala.concurrent.duration._

/**
 * Created by zhange on 2016-05-18.
 *
 */

object Boot extends App {

  import proservers.cores.SpiderDispatcher

  implicit val system = ActorSystem("DispatcherSystem")
  import akka.actor.ActorRef
  import system.dispatcher

  val spiderDispatcher = system.actorOf(SpiderDispatcher.props, name = "SpiderDispatcher")
  println(s"Binding on local path: ${spiderDispatcher.path}")

}
