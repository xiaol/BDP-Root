package commons.models.news

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
 * Created by zhange on 2016-05-04.
 *
 */

trait NewsBodyBlock

case class TextBlock(txt: String) extends NewsBodyBlock

object TextBlock {
  implicit val TextBlockFormat: Format[TextBlock] =
    (__ \ "txt").format[String].inmap(txt => TextBlock(txt), (textBlock: TextBlock) => textBlock.txt)
}

case class ImageBlock(img: String) extends NewsBodyBlock

object ImageBlock {
  implicit val ImageBlockFormat: Format[ImageBlock] =
    (__ \ "img").format[String].inmap(img => ImageBlock(img), (imageBlock: ImageBlock) => imageBlock.img)
}

case class VideoBlock(vid: String) extends NewsBodyBlock

object VideoBlock {
  implicit val VideoBlockFormat: Format[VideoBlock] =
    (__ \ "vid").format[String].inmap(vid => VideoBlock(vid), (videoBlock: VideoBlock) => videoBlock.vid)
}

object NewsBodyBlock {
  implicit val NewsBodyBlockWrites = Writes[NewsBodyBlock] {
    case textBlock: TextBlock   => Json.writes[TextBlock].writes(textBlock)
    case imageBlock: ImageBlock => Json.writes[ImageBlock].writes(imageBlock)
    case videoBlock: VideoBlock => Json.writes[VideoBlock].writes(videoBlock)
  }

  implicit val NewsBodyBlockReads = {
    val txtReads = Json.reads[TextBlock]
    val imgReads = Json.reads[ImageBlock]
    val vidReads = Json.reads[VideoBlock]
    __.read[TextBlock](txtReads).map(x => x: NewsBodyBlock) |
      __.read[ImageBlock](imgReads).map(x => x: NewsBodyBlock) |
      __.read[VideoBlock](vidReads).map(x => x: NewsBodyBlock)
  }
}