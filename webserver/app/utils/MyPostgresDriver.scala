package utils

import com.github.tminglei.slickpg.utils.SimpleArrayUtils._
import com.github.tminglei.slickpg.{ PgArraySupport, _ }
import play.api.libs.json._

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait MyPostgresDriver extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupportJoda
    with PgNetSupport
    with PgLTreeSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgPlayJsonSupport
    with array.PgArrayJdbcTypes {

  override val pgjson = "jsonb"

  override val api = new API with DateTimeImplicits with ArrayImplicits with NetImplicits with LTreeImplicits with RangeImplicits with HStoreImplicits with SearchImplicits with SearchAssistants with CustomArrayImplicitsPlus with JsonImplicits {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val json4sJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)
  }

  val plainAPI = new API with PlayJsonPlainImplicits with JodaDateTimePlainImplicits

  trait CustomArrayImplicitsPlus {
    implicit val simpleLongBufferTypeMapper = new SimpleArrayJdbcType[Long]("int8").to(_.toBuffer)
    implicit val simpleStrVectorTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toVector)
    implicit val advancedStringListTypeMapper = new AdvancedArrayJdbcType[String]("text",
      fromString(identity)(_).orNull, mkString(identity))
  }
}

object MyPostgresDriver extends MyPostgresDriver

