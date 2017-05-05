package commons.models.advertisement

import org.joda.time.LocalDateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ Reads, JsPath, Writes }

/**
 * Created by zhangshl on 16/11/24.
 */

case class AdRequest(device: Device,
                     impression: List[Impression],
                     ts: String,
                     version: Option[String] = None,
                     extend_data: Option[String] = None)

object AdRequest {
  implicit val AdRequestWrites: Writes[AdRequest] = (
    (JsPath \ "device").write[Device] ~
    (JsPath \ "impression").write[List[Impression]] ~
    (JsPath \ "ts").write[String] ~
    (JsPath \ "version").writeNullable[String] ~
    (JsPath \ "extend_data").writeNullable[String]
  )(unlift(AdRequest.unapply))

  implicit val AdRequestReads: Reads[AdRequest] = (
    (JsPath \ "device").read[Device] ~
    (JsPath \ "impression").read[List[Impression]] ~
    (JsPath \ "ts").read[String] ~
    (JsPath \ "version").readNullable[String] ~
    (JsPath \ "extend_data").readNullable[String]
  )(AdRequest.apply _)
}

case class Impression(aid: String,
                      width: Option[String] = None,
                      height: Option[String] = None,
                      keywords: Option[String] = None,
                      page_index: Option[String] = None,
                      page_size: Option[String] = None)

object Impression {
  implicit val ImpressionWrites: Writes[Impression] = (
    (JsPath \ "aid").write[String] ~
    (JsPath \ "width").writeNullable[String] ~
    (JsPath \ "height").writeNullable[String] ~
    (JsPath \ "keywords").writeNullable[String] ~
    (JsPath \ "page_index").writeNullable[String] ~
    (JsPath \ "page_size").writeNullable[String]
  )(unlift(Impression.unapply))

  implicit val ImpressionReads: Reads[Impression] = (
    (JsPath \ "aid").read[String] ~
    (JsPath \ "width").readNullable[String] ~
    (JsPath \ "height").readNullable[String] ~
    (JsPath \ "keywords").readNullable[String] ~
    (JsPath \ "page_index").readNullable[String] ~
    (JsPath \ "page_size").readNullable[String]
  )(Impression.apply _)
}

case class Device(ip: Option[String] = None,
                  imei: Option[String] = None,
                  imeiori: Option[String] = None,
                  mac: Option[String] = None,
                  macori: Option[String] = None,
                  mac1: Option[String] = None,
                  idfa: Option[String] = None,
                  idfaori: Option[String] = None,
                  aaid: Option[String] = None,
                  anid: Option[String] = None,
                  anidori: Option[String] = None,
                  udid: Option[String] = None,
                  //                  duid: Option[String] = None,
                  brand: Option[String] = None,
                  platform: Option[String] = None,
                  os: Option[String] = None,
                  os_version: Option[String] = None,
                  device_size: Option[String] = None,
                  network: Option[String] = None,
                  operator: Option[String] = None,
                  longitude: Option[String] = None,
                  latitude: Option[String] = None,
                  screen_orientation: Option[String] = None)

object Device {
  implicit val DeviceWrites: Writes[Device] = (
    (JsPath \ "ip").writeNullable[String] ~
    (JsPath \ "imei").writeNullable[String] ~
    (JsPath \ "imeiori").writeNullable[String] ~
    (JsPath \ "mac").writeNullable[String] ~
    (JsPath \ "macori").writeNullable[String] ~
    (JsPath \ "mac1").writeNullable[String] ~
    (JsPath \ "idfa").writeNullable[String] ~
    (JsPath \ "idfaori").writeNullable[String] ~
    (JsPath \ "aaid").writeNullable[String] ~
    (JsPath \ "anid").writeNullable[String] ~
    (JsPath \ "anidori").writeNullable[String] ~
    (JsPath \ "udid").writeNullable[String] ~
    //    (JsPath \ "duid").writeNullable[String] ~
    (JsPath \ "brand").writeNullable[String] ~
    (JsPath \ "platform").writeNullable[String] ~
    (JsPath \ "os").writeNullable[String] ~
    (JsPath \ "os_version").writeNullable[String] ~
    (JsPath \ "device_size").writeNullable[String] ~
    (JsPath \ "network").writeNullable[String] ~
    (JsPath \ "operator").writeNullable[String] ~
    (JsPath \ "longitude").writeNullable[String] ~
    (JsPath \ "latitude").writeNullable[String] ~
    (JsPath \ "screen_orientation").writeNullable[String]
  )(unlift(Device.unapply))

  implicit val DeviceReads: Reads[Device] = (
    (JsPath \ "ip").readNullable[String] ~
    (JsPath \ "imei").readNullable[String] ~
    (JsPath \ "imeiori").readNullable[String] ~
    (JsPath \ "mac").readNullable[String] ~
    (JsPath \ "macori").readNullable[String] ~
    (JsPath \ "mac1").readNullable[String] ~
    (JsPath \ "idfa").readNullable[String] ~
    (JsPath \ "idfaori").readNullable[String] ~
    (JsPath \ "aaid").readNullable[String] ~
    (JsPath \ "anid").readNullable[String] ~
    (JsPath \ "anidori").readNullable[String] ~
    (JsPath \ "udid").readNullable[String] ~
    //    (JsPath \ "duid").readNullable[String] ~
    (JsPath \ "brand").readNullable[String] ~
    (JsPath \ "platform").readNullable[String] ~
    (JsPath \ "os").readNullable[String] ~
    (JsPath \ "os_version").readNullable[String] ~
    (JsPath \ "device_size").readNullable[String] ~
    (JsPath \ "network").readNullable[String] ~
    (JsPath \ "operator").readNullable[String] ~
    (JsPath \ "longitude").readNullable[String] ~
    (JsPath \ "latitude").readNullable[String] ~
    (JsPath \ "screen_orientation").readNullable[String]
  )(Device.apply _)
}

case class Phone(uid: Long,
                 b: String,
                 ctype: Int,
                 province: Option[String] = None,
                 city: Option[String] = None,
                 area: Option[String] = None,
                 ptype: Int,
                 appversion: Option[String] = None)

object Phone {
  implicit val PhoneReads: Reads[Phone] = (
    (JsPath \ "uid").read[Long] ~
    (JsPath \ "b").read[String] ~
    (JsPath \ "ctype").read[Int] ~
    (JsPath \ "province").readNullable[String] ~
    (JsPath \ "city").readNullable[String] ~
    (JsPath \ "area").readNullable[String] ~
    (JsPath \ "ptype").read[Int] ~
    (JsPath \ "appversion").readNullable[String]
  )(Phone.apply _)
}

case class Slide(mid: String,
                 uid: Long,
                 ctype: Int,
                 ptype: Int,
                 ctime: Option[LocalDateTime] = None,
                 ipaddress: Option[String] = None,
                 version_text: Option[String] = None,
                 operate_type: Option[Int] = None)