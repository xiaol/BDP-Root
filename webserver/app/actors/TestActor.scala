package actors

import akka.actor.Actor
import akka.actor.Props

/**
 * Created by zhange on 2016-05-17.
 *
 */

class TestActor extends Actor {
  import TestActor._

  override def receive = {
    case HelloActor(msg) => sender ! s"Get $msg"
    case msg: String =>
      println(s"UnKnownMsg: $msg"); sender ! s"UnKnownMsg: $msg"
    case _ => sender ! "UnKnownMsg"
  }

}

object TestActor {
  def props = Props[TestActor]

  case class HelloActor(msg: String)
}
