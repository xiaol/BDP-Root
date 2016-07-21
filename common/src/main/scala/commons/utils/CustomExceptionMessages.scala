package commons.utils

/**
 * Created by zhange on 2016-07-15.
 *
 */

trait ExceptionMessage

trait DBExceptionMessage extends ExceptionMessage

case class NotFound(table: String, fieldPairs: (String, String)*) extends DBExceptionMessage {
  override def toString: String =
    s"${table.capitalize} not found by ${fieldPairs.map { case (k, v) => s"$k == $v" }.mkString(", ")}"
}

case class AlreadyExist(table: String, row: String) extends DBExceptionMessage {
  override def toString: String = s"${table.capitalize} already has row: $row"
}

case class ExecutionFail(msg: String) extends DBExceptionMessage {
  override def toString: String = msg
}
