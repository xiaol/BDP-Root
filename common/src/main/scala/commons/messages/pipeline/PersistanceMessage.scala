package commons.messages.pipeline

/**
 * Created by zhange on 2016-05-21.
 *
 */

trait PersistanceMessage

case class PersistanceReply(key: String)

case class PersisInsert[T](data: T)

