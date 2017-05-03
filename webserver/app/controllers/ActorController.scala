package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

import play.api.mvc._
import akka.actor._
import javax.inject._

import actors.SpiderDispatcherServer

/**
 * Created by zhange on 2016-05-17.
 *
 */
@Singleton
class ActorController @Inject() extends Controller {}
//@Singleton
//class ActorController @Inject() (system: ActorSystem) extends Controller {
//
//  implicit val timeout: Timeout = 5.seconds
//
//  //  val testActor = system.actorOf(TestActor.props, "test-actor")
//  //
//  //  def sayHello(msg: String) = Action.async {
//  //    (testActor ? HelloActor(msg)).mapTo[String].map { message =>
//  //      Ok(message)
//  //    }
//  //  }
//
//  val remoteActor = system.actorOf(SpiderDispatcherServer.props)
//
//  def helloRemote(msg: String) = Action.async {
//    import commons.messages.dispatcher.DispatchTest
//    (remoteActor ? DispatchTest(msg)).mapTo[String].map { message =>
//      Ok(message)
//    }
//  }
//}
