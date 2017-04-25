package utils

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{ JsPath, Json, Reads, Writes }
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import utils.Response._

/**
 * Created by fengjigang on 17/4/25.
 */
case class AdSourceResponse[T](code: Int, data: T, feedAdPos: Int, relatedAdPos: Int)

object AdSourceResponse {

  implicit def ResponseWrites[T: Writes]: Writes[AdSourceResponse[T]] = (
    (JsPath \ "code").write[Int] ~
    (JsPath \ "data").write[T] ~
    (JsPath \ "feedAdPos").write[Int] ~
    (JsPath \ "relatedAdPos").write[Int]
  )(unlift(AdSourceResponse.unapply[T]))

  implicit def ResponseReads[T: Reads]: Reads[AdSourceResponse[T]] = (
    (JsPath \ "code").read[Int] ~
    (JsPath \ "data").read[T] ~
    (JsPath \ "feedAdPos").read[Int] ~
    (JsPath \ "relatedAdPos").read[Int]
  )(AdSourceResponse.apply[T] _)

  def ServerSucced[T](data: T, feedAdPos: Int, relatedAdPos: Int)(implicit writes: Writes[T]): Result = {
    Ok(Json.toJson(AdSourceResponse(SERVER_SUCCED_CODE, data, feedAdPos, relatedAdPos)))
  }
}
