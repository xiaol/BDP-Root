package commons.models.advertisement

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads, Writes }

/**
 * Created by zhangshl on 16/8/31.
 */

case class Event(event_key: Option[Int] = None,
                 event_value: Option[String] = None)

object Event {
  implicit val EventWrites: Writes[Event] = (
    (JsPath \ "event_key").writeNullable[Int] ~
    (JsPath \ "event_value").writeNullable[String]
  )(unlift(Event.unapply))

  implicit val EventReads: Reads[Event] = (
    (JsPath \ "event_key").readNullable[Int] ~
    (JsPath \ "event_value").readNullable[String]
  )(Event.apply _)
}

case class Tracking(tracking_key: Option[Int] = None,
                    tracking_value: Option[List[String]] = None)
object Tracking {
  implicit val TrackingWrites: Writes[Tracking] = (
    (JsPath \ "tracking_key").writeNullable[Int] ~
    (JsPath \ "tracking_value").writeNullable[List[String]]
  )(unlift(Tracking.unapply))

  implicit val TrackingReads: Reads[Tracking] = (
    (JsPath \ "tracking_key").readNullable[Int] ~
    (JsPath \ "tracking_value").readNullable[List[String]]
  )(Tracking.apply _)
}

case class App(app_name: Option[String] = None,
               app_package: Option[String] = None,
               app_category: Option[String] = None,
               app_size: Option[Int] = None)
object App {
  implicit val AppWrites: Writes[App] = (
    (JsPath \ "app_name").writeNullable[String] ~
    (JsPath \ "app_package").writeNullable[String] ~
    (JsPath \ "app_category").writeNullable[String] ~
    (JsPath \ "app_size").writeNullable[Int]
  )(unlift(App.unapply))

  implicit val AppReads: Reads[App] = (
    (JsPath \ "app_name").readNullable[String] ~
    (JsPath \ "app_package").readNullable[String] ~
    (JsPath \ "app_category").readNullable[String] ~
    (JsPath \ "app_size").readNullable[Int]
  )(App.apply _)
}

case class Ad_native(template_id: Option[String] = None,
                     index: Option[Int] = None,
                     node_name: Option[String] = None,
                     required_field: Option[Int] = None,
                     action_type: Option[Int] = None,
                     action_value: Option[String] = None,
                     required_value: Option[String] = None,
                     ptype: Option[String] = None,
                     index_value: Option[String] = None)
object Ad_native {
  implicit val Ad_nativeWrites: Writes[Ad_native] = (
    (JsPath \ "template_id").writeNullable[String] ~
    (JsPath \ "index").writeNullable[Int] ~
    (JsPath \ "node_name").writeNullable[String] ~
    (JsPath \ "required_field").writeNullable[Int] ~
    (JsPath \ "action_type").writeNullable[Int] ~
    (JsPath \ "action_value").writeNullable[String] ~
    (JsPath \ "required_value").writeNullable[String] ~
    (JsPath \ "type").writeNullable[String] ~
    (JsPath \ "index_value").writeNullable[String]
  )(unlift(Ad_native.unapply))

  implicit val Ad_nativeReads: Reads[Ad_native] = (
    (JsPath \ "template_id").readNullable[String] ~
    (JsPath \ "index").readNullable[Int] ~
    (JsPath \ "node_name").readNullable[String] ~
    (JsPath \ "required_field").readNullable[Int] ~
    (JsPath \ "action_type").readNullable[Int] ~
    (JsPath \ "action_value").readNullable[String] ~
    (JsPath \ "required_value").readNullable[String] ~
    (JsPath \ "type").readNullable[String] ~
    (JsPath \ "index_value").readNullable[String]
  )(Ad_native.apply _)
}

case class Video(width: Option[Int] = None,
                 height: Option[Int] = None,
                 ptype: Option[String] = None,
                 duration: Option[Int] = None,
                 creative_url: Option[String] = None)
object Video {
  implicit val VideoWrites: Writes[Video] = (
    (JsPath \ "width").writeNullable[Int] ~
    (JsPath \ "height").writeNullable[Int] ~
    (JsPath \ "type").writeNullable[String] ~
    (JsPath \ "duration").writeNullable[Int] ~
    (JsPath \ "creative_url").writeNullable[String]
  )(unlift(Video.unapply))

  implicit val VideoReads: Reads[Video] = (
    (JsPath \ "width").readNullable[Int] ~
    (JsPath \ "height").readNullable[Int] ~
    (JsPath \ "type").readNullable[String] ~
    (JsPath \ "duration").readNullable[Int] ~
    (JsPath \ "creative_url").readNullable[String]
  )(Video.apply _)
}

case class Banner(width: Option[Int] = None,
                  height: Option[Int] = None,
                  ptype: Option[String] = None,
                  creative_url: Option[String] = None)
object Banner {
  implicit val BannerWrites: Writes[Banner] = (
    (JsPath \ "width").writeNullable[Int] ~
    (JsPath \ "height").writeNullable[Int] ~
    (JsPath \ "type").writeNullable[String] ~
    (JsPath \ "creative_url").writeNullable[String]
  )(unlift(Banner.unapply))

  implicit val BannerReads: Reads[Banner] = (
    (JsPath \ "width").readNullable[Int] ~
    (JsPath \ "height").readNullable[Int] ~
    (JsPath \ "type").readNullable[String] ~
    (JsPath \ "creative_url").readNullable[String]
  )(Banner.apply _)
}

case class Creative(cid: Option[Int] = None,
                    is_html: Option[Boolean] = None,
                    index: Option[Int] = None,
                    banner: Option[Banner] = None,
                    video: Option[Video] = None,
                    ad_native: Option[List[Ad_native]] = None,
                    html_snippet: Option[String] = None,
                    app: Option[App] = None,
                    impression: Option[List[String]] = None,
                    click: Option[List[String]] = None,
                    tracking: Option[List[Tracking]] = None,
                    event: Option[List[Event]] = None,
                    admark: Option[String] = None)
object Creative {
  implicit val CreativeWrites: Writes[Creative] = (
    (JsPath \ "cid").writeNullable[Int] ~
    (JsPath \ "is_html").writeNullable[Boolean] ~
    (JsPath \ "index").writeNullable[Int] ~
    (JsPath \ "banner").writeNullable[Banner] ~
    (JsPath \ "video").writeNullable[Video] ~
    (JsPath \ "ad_native").writeNullable[List[Ad_native]] ~
    (JsPath \ "html_snippet").writeNullable[String] ~
    (JsPath \ "app").writeNullable[App] ~
    (JsPath \ "impression").writeNullable[List[String]] ~
    (JsPath \ "click").writeNullable[List[String]] ~
    (JsPath \ "tracking").writeNullable[List[Tracking]] ~
    (JsPath \ "event").writeNullable[List[Event]] ~
    (JsPath \ "admark").writeNullable[String]
  )(unlift(Creative.unapply))

  implicit val CreativeReads: Reads[Creative] = (
    (JsPath \ "cid").readNullable[Int] ~
    (JsPath \ "is_html").readNullable[Boolean] ~
    (JsPath \ "index").readNullable[Int] ~
    (JsPath \ "banner").readNullable[Banner] ~
    (JsPath \ "video").readNullable[Video] ~
    (JsPath \ "ad_native").readNullable[List[Ad_native]] ~
    (JsPath \ "html_snippet").readNullable[String] ~
    (JsPath \ "app").readNullable[App] ~
    (JsPath \ "impression").readNullable[List[String]] ~
    (JsPath \ "click").readNullable[List[String]] ~
    (JsPath \ "tracking").readNullable[List[Tracking]] ~
    (JsPath \ "event").readNullable[List[Event]] ~
    (JsPath \ "admark").readNullable[String]
  )(Creative.apply _)
}

case class Adspace(aid: Option[Int] = None,
                   adformat: Option[Int] = None,
                   creative: Option[List[Creative]] = None)
object Adspace {
  implicit val AdspaceWrites: Writes[Adspace] = (
    (JsPath \ "aid").writeNullable[Int] ~
    (JsPath \ "adformat").writeNullable[Int] ~
    (JsPath \ "creative").writeNullable[List[Creative]]
  )(unlift(Adspace.unapply))

  implicit val AdspaceReads: Reads[Adspace] = (
    (JsPath \ "aid").readNullable[Int] ~
    (JsPath \ "adformat").readNullable[Int] ~
    (JsPath \ "creative").readNullable[List[Creative]]
  )(Adspace.apply _)
}

case class Data(version: Option[Float] = None,
                adspace: Option[List[Adspace]] = None,
                extend_data: Option[String] = None)
object Data {
  implicit val DataWrites: Writes[Data] = (
    (JsPath \ "version").writeNullable[Float] ~
    (JsPath \ "adspace").writeNullable[List[Adspace]] ~
    (JsPath \ "extend_data").writeNullable[String]
  )(unlift(Data.unapply))

  implicit val DataReads: Reads[Data] = (
    (JsPath \ "version").readNullable[Float] ~
    (JsPath \ "adspace").readNullable[List[Adspace]] ~
    (JsPath \ "extend_data").readNullable[String]
  )(Data.apply _)
}

case class AdResponse(version: Option[Float] = None,
                      status: Option[Int] = None,
                      message: Option[String] = None,
                      data: Option[Data] = None)
object AdResponse {
  implicit val AdResponseWrites: Writes[AdResponse] = (
    (JsPath \ "version").writeNullable[Float] ~
    (JsPath \ "status").writeNullable[Int] ~
    (JsPath \ "message").writeNullable[String] ~
    (JsPath \ "data").writeNullable[Data]
  )(unlift(AdResponse.unapply))

  implicit val AdResponseReads: Reads[AdResponse] = (
    (JsPath \ "version").readNullable[Float] ~
    (JsPath \ "status").readNullable[Int] ~
    (JsPath \ "message").readNullable[String] ~
    (JsPath \ "data").readNullable[Data]
  )(AdResponse.apply _)
}