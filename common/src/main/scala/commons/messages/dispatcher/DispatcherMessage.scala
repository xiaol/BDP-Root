package commons.messages.dispatcher

import commons.models.spiders.SourceRow
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Writes }

/**
 * Created by zhange on 2016-05-18.
 *
 */

trait DispatcherMessage

case class DispatcherMeta(cid: Long, sname: String, state: Int, pconf: Option[String]) extends DispatcherMessage

case class DispatcherTask(sid: Long, surl: Option[String], meta: DispatcherMeta) extends DispatcherMessage

case class DispatcherInit(sid: Long, queue: String, rate: Int, status: Int, task: DispatcherTask) extends DispatcherMessage

case class PushDispatcherTask(init: DispatcherInit) extends DispatcherMessage

case class StartDispatcher(init: DispatcherInit) extends DispatcherMessage

case class CloseDispatcher(init: DispatcherInit) extends DispatcherMessage

case class ReloadDispatcher(init: DispatcherInit) extends DispatcherMessage

case class StartDispatchers(inits: List[DispatcherInit]) extends DispatcherMessage

case class CloseDispatchers(inits: List[DispatcherInit]) extends DispatcherMessage

case class ReloadDispatchers(inits: List[DispatcherInit]) extends DispatcherMessage

case class DispatchResponse(status: Int, task: DispatcherTask) extends DispatcherMessage
case class DispatchResponses(results: List[DispatchResponse]) extends DispatcherMessage

case class DispatchTest(msg: String) extends DispatcherMessage

object DispatcherInit {
  def apply(source: SourceRow): DispatcherInit = {
    val pconf: Option[String] = source.pconf.map { p => p.toString }
    val meta: DispatcherMeta = DispatcherMeta(source.cid, source.sname, source.state, pconf)
    val task: DispatcherTask = DispatcherTask(source.id.get, source.surl, meta)
    new DispatcherInit(source.id.get, source.queue, source.rate, source.status, task)
  }
}

object DispatcherMeta {
  implicit val DispatcherMetaWrites: Writes[DispatcherMeta] = (
    (JsPath \ "cid").write[Long] ~
    (JsPath \ "sname").write[String] ~
    (JsPath \ "state").write[Int] ~
    (JsPath \ "pconf").writeNullable[String]
  )(unlift(DispatcherMeta.unapply))
}

object DispatcherTask {
  implicit val DispatcherTaskWrites: Writes[DispatcherTask] = (
    (JsPath \ "sid").write[Long] ~
    (JsPath \ "surl").writeNullable[String] ~
    (JsPath \ "meta").write[DispatcherMeta]
  )(unlift(DispatcherTask.unapply))
}

object DispatchResponse {
  implicit val DispatchResponseWrites: Writes[DispatchResponse] = (
    (JsPath \ "status").write[Int] ~
    (JsPath \ "task").write[DispatcherTask]
  )(unlift(DispatchResponse.unapply))

  def apply(init: DispatcherInit): DispatchResponse = new DispatchResponse(init.status, init.task)
}