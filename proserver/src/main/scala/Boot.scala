import akka.actor.{ ActorRef, ActorSystem }
import akka.routing.FromConfig
import proservers.cores._
import proservers.cores.SpiderNewsPipelineServer
import akka.stream._

/**
 * Created by zhange on 2016-05-19.
 *
 */

object Boot extends App {

  implicit val system = ActorSystem("ProServer")
  val decider: Supervision.Decider = { case _ => Supervision.Resume }
  implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  val newsPipelineRoutees: ActorRef = system.actorOf(FromConfig.props(), "NewsPipelineRoutees")

  // persistance server
  val persistanceServer: ActorRef = system.actorOf(PersistanceServer.props, "PersistanceServer")

  val imageProcessServer: ActorRef = system.actorOf(ImageProcessServer.props, "ImageProcessServer")

  val imageServer: ActorRef = system.actorOf(ImagePipelineServer.props(imageProcessServer), "ImagePipelineServer")

  // news pipeline server
  val newsPipelineServer: ActorRef = system.actorOf(SpiderNewsPipelineServer.props(persistanceServer, imageServer), "SpiderNewsPipelineServer")

}
