package commons.utils

import CustomExceptions._

/**
 * Created by zhange on 2016-07-14.
 *
 */

trait CustomExceptions

object CustomExceptions {
  def defaultMessage[T](message: Option[T], cause: Option[Throwable]) = {
    (message, cause) match {
      case (Some(msg), _) => msg.toString
      case (_, Some(thr)) => thr.toString
      case _              => null
    }
  }
}

case class PGDBException(message: Option[DBExceptionMessage], cause: Option[Throwable] = None) extends RuntimeException(defaultMessage(message, cause)) with CustomExceptions {
  def getErrorEntity: DBExceptionMessage = message.getOrElse(ExecutionFail("Unknown Error."))
}

object PGDBException {
  def apply(message: DBExceptionMessage) = new PGDBException(Some(message), None)
  def apply(throwable: Throwable) = new PGDBException(None, Some(throwable))
}

