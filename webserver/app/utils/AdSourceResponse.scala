package utils

import play.api.libs.functional.syntax.{ unlift, _ }
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsPath, Json, Reads, Writes }
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import utils.Response._

/**
 * Created by fengjigang on 17/4/25.
 */
case class AdSourceResponse[T](code: Int, data: T, feedAdPos: Int, relatedAdPos: Int, feedVideoAdPos: Int, relatedVideoAdPos: Int)

object AdSourceResponse {

  implicit def ResponseWrites[T: Writes]: Writes[AdSourceResponse[T]] = (
    (JsPath \ "code").write[Int] ~
    (JsPath \ "data").write[T] ~
    (JsPath \ "feedAdPos").write[Int] ~
    (JsPath \ "relatedAdPos").write[Int] ~
    (JsPath \ "feedVideoAdPos").write[Int] ~
    (JsPath \ "relatedVideoAdPos").write[Int]
  )(unlift(AdSourceResponse.unapply[T]))

  implicit def ResponseReads[T: Reads]: Reads[AdSourceResponse[T]] = (
    (JsPath \ "code").read[Int] ~
    (JsPath \ "data").read[T] ~
    (JsPath \ "feedAdPos").read[Int] ~
    (JsPath \ "relatedAdPos").read[Int] ~
    (JsPath \ "feedVideoAdPos").read[Int] ~
    (JsPath \ "relatedVideoAdPos").read[Int]
  )(AdSourceResponse.apply[T] _)

  def ServerSucced[T](data: T, feedAdPos: Int, relatedAdPos: Int, feedVideoAdPos: Int, relatedVideoAdPos: Int)(implicit writes: Writes[T]): Result = {
    Ok(Json.toJson(AdSourceResponse(SERVER_SUCCED_CODE, data, feedAdPos, relatedAdPos, feedVideoAdPos, relatedVideoAdPos)))
  }
}
