package utils

import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.libs.json.JsError
import play.api.mvc.Results._
import play.api.mvc.Result

/**
 * Created by zhange on 2016-04-24.
 *
 */

case class Response[T](code: Int, data: T)

object Response {
  implicit def ResponseWrites[T: Writes] = new Writes[Response[T]] {
    def writes(rep: Response[T]) = Json.obj(
      "code" -> rep.code,
      "data" -> rep.data
    )
  }

  private final val SERVER_SUCCED_CODE = 2000
  private final val SERVER_ERROR_CODE = 2001
  private final val SERVER_EMPTY_CODE = 2002
  private final val SERVER_CREATE_CODE = 2003
  private final val SERVER_DELETE_CODE = 2004

  private final val DATA_INVALID_CODE = 4001
  private final val JSON_INVALID_CODE = 4002
  private final val AUTH_VERIFY_CODE = 4003

  def ServerSucced[T](data: T)(implicit writes: Writes[T]): Result = {
    Ok(Json.toJson(Response(SERVER_SUCCED_CODE, data)))
  }

  def ServerError(data: String): Result = {
    Ok(Json.toJson(Response(SERVER_ERROR_CODE, s"InternalServerError: $data")))
  }

  def DataEmptyError(data: String): Result = {
    Ok(Json.toJson(Response(SERVER_EMPTY_CODE, s"NotFoundError: $data")))
  }

  def DataCreateError(data: String): Result = {
    Ok(Json.toJson(Response(SERVER_CREATE_CODE, s"CreateDataError: $data")))
  }

  def DataDeleteError(data: String): Result = {
    Ok(Json.toJson(Response(SERVER_DELETE_CODE, s"DeleteDataError: $data")))
  }

  def DataInvalidError(data: String): Result = {
    Ok(Json.toJson(Response(DATA_INVALID_CODE, s"InvalidDataError: $data")))
  }

  def JsonInvalidError(data: JsError): Result = {
    Ok(Json.toJson(Response(JSON_INVALID_CODE, JsError.toJson(data).toString)))
  }

  def AuthVerifyError(data: String): Result = {
    Ok(Json.toJson(Response(AUTH_VERIFY_CODE, s"AuthVerifyError: $data")))
  }
}