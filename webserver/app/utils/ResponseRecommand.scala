package utils

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

/**
 * Created by zhangshl on 16/7/20.
 */

case class ResponseRecommand[T](code: Int, data: T, total: Option[Long] = None, data2: Option[T] = None)

object ResponseRecommand {

  implicit def ResponseRecommandWrites[T: Writes]: Writes[ResponseRecommand[T]] = (
    (JsPath \ "code").write[Int] ~
    (JsPath \ "data").write[T] ~
    (JsPath \ "total").writeNullable[Long] ~
    (JsPath \ "publish").writeNullable[T]
  )(unlift(ResponseRecommand.unapply[T]))

  implicit def ResponseRecommandReads[T: Reads]: Reads[ResponseRecommand[T]] = (
    (JsPath \ "code").read[Int] ~
    (JsPath \ "data").read[T] ~
    (JsPath \ "total").readNullable[Long] ~
    (JsPath \ "publish").readNullable[T]
  )(ResponseRecommand.apply[T] _)

  private final val SERVER_SUCCED_CODE = 2000
  private final val SERVER_ERROR_CODE = 2001
  private final val SERVER_EMPTY_CODE = 2002
  private final val SERVER_CREATE_CODE = 2003
  private final val SERVER_DELETE_CODE = 2004

  private final val DATA_INVALID_CODE = 4001
  private final val JSON_INVALID_CODE = 4002
  private final val AUTH_VERIFY_CODE = 4003

  def ServerSucced[T](data: T, total: Option[Long] = None, data2: Option[T] = None)(implicit writes: Writes[T]): Result = {
    Ok(Json.toJson(ResponseRecommand(SERVER_SUCCED_CODE, data, total, data2)))
  }

  def ServerError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(SERVER_ERROR_CODE, s"InternalServerError: $data")))
  }

  def DataEmptyError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(SERVER_EMPTY_CODE, s"NotFoundError: $data")))
  }

  def DataCreateError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(SERVER_CREATE_CODE, s"CreateDataError: $data")))
  }

  def DataDeleteError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(SERVER_DELETE_CODE, s"DeleteDataError: $data")))
  }

  def DataInvalidError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(DATA_INVALID_CODE, s"InvalidDataError: $data")))
  }

  def JsonInvalidError(data: JsError): Result = {
    Ok(Json.toJson(ResponseRecommand(JSON_INVALID_CODE, JsError.toJson(data).toString)))
  }

  def AuthVerifyError(data: String): Result = {
    Ok(Json.toJson(ResponseRecommand(AUTH_VERIFY_CODE, s"AuthVerifyError: $data")))
  }
}