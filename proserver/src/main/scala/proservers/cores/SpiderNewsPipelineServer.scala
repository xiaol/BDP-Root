package proservers.cores

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import akka.actor._
import akka.pattern.ask
import akka.event.{ Logging, LoggingAdapter }
import commons.utils.JodaUtils._

import scala.util.control.NonFatal
import play.api.libs.json._
import org.joda.time.LocalDateTime
import commons.models.news._
import SpiderNewsPipelineServer._
import akka.stream._
import proservers.utils.RedisDriver.cache
import commons.messages.pipeline._
import commons.models.community._
import commons.models.news.NewsBodyBlock
import commons.models.spiders._
import commons.utils.Md5Utils.md5Hash
import proservers.webservices.ASearchClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zhange on 2016-05-19.
 *
 */

class SpiderNewsPipelineServer(persistanceServer: ActorRef, imageServer: ActorRef)(implicit val mat: Materializer) extends Actor {

  implicit val contextSystem = context.system
  implicit val ec: ExecutionContext = contextSystem.dispatcher
  implicit val logger: LoggingAdapter = Logging(contextSystem, this.getClass)

  override def receive = {
    case NewsPipelineTask(task) =>
      val superior = sender()
      verifyNewsTemp(task).onComplete {
        case Success(Right(newsTemp)) =>
          verifyNewsUnique(newsTemp.base.url, newsTemp.base.title, newsTemp.base.docid).onComplete {
            case Success(true) =>
              processNewsPipeline(task, newsTemp); superior ! NewsPipelineTask(task)
            case Success(false) => superior ! s"CacheUniqueErr: $task"
            case Failure(err) =>
              logger.error(s"SpiderNewsPipelineServer.verifyNewsUnique($task): ${err.getMessage}")
              superior ! s"CacheUniqueErr: $task, ${err.getMessage}"
          }
        case Success(Left(verifyErrMsg)) =>
          superior ! verifyErrMsg
          logger.error(s"SpiderNewsPipelineServer.verifyNewsTemp($task): $verifyErrMsg")
        case Failure(err) =>
          logger.error(s"SpiderNewsPipelineServer.verifyNewsTemp($task): ${err.getMessage}")
          superior ! s"CacheVerifyErr: $task, ${err.getMessage}"
      }
    case msg: String => sender ! s"Pipeline: $msg"
    case msg @ _     => logger.error(s"UnCatchMsg: $msg")
  }

  def processNewsPipeline(task: String, temp: NewsTemp): Unit = {
    context.actorOf(Props(new Actor() {

      val imageNumber: Int = temp.base.content.collect { case ImageBlock(_) => 1 }.sum
      val processPeriod = ImageTask.assessProcessPeriod(imageNumber, 200)
      val timer: Cancellable = context.system.scheduler.scheduleOnce(processPeriod.seconds, self, ReceiveTimeout)
      // val checker: Cancellable = context.system.scheduler.schedule(30.seconds, 30.seconds, self, ChcekReadyInTime)

      setUniqueLock(temp.base.url, temp.base.title, temp.base.docid, PROCE_LOCK, processPeriod)

      var searchTemp: Option[List[ASearch]] = None
      var feedOSSTemp: Option[List[String]] = None
      var detailOSSTemp: Option[List[Oss]] = None

      override def receive = {
        case DetailOssList(osss) =>
          detailOSSTemp = Some(osss); imageServer ! FeedTasks(osss.map(_.oss))
        case FeedOssList(srcs) => feedOSSTemp = Some(srcs)
        case ReceiveTimeout    => storeNewsRow()
        case ChcekReadyInTime =>
          if (searchTemp.isDefined && detailOSSTemp.isDefined && feedOSSTemp.isDefined) storeNewsRow()
        case msg @ _ => logger.error(s"UnCatchMsg: $msg")
      }

      temp.base.content.collect { case ImageBlock(src) if src.startsWith("http") => src } match {
        case srcs: List[String] if srcs.nonEmpty => imageServer ! DetailTasks(srcs)
        case srcs: List[String] =>
          detailOSSTemp = Some(List[Oss]()); feedOSSTemp = Some(List[String]())
      }

      ASearchClient().getASearchs(temp.base.title).onComplete {
        case Success(Some(aSearchs)) => searchTemp = Some(aSearchs.aSearchs)
        case _                       => searchTemp = Some(List[ASearch]())
      }

      def storeNewsRow() = {
        formatNewsRow(temp, feedOSSTemp, detailOSSTemp) match {
          case None => shutdown()
          case Some(row) => (persistanceServer ? row)(15.seconds).recover { case _ => None }.map {
            case Some(nid) =>
              setUniqueLock(temp.base.url, temp.base.title, temp.base.docid, CACHE_LOCK, CACHE_LIMITED)
              if (temp.syst.comment_queue.isDefined && temp.syst.comment_task.isDefined) {
                cache.lpush(temp.syst.comment_queue.get, temp.syst.comment_task.get)
              }
              if (searchTemp.isDefined && searchTemp.get.nonEmpty)
                persistanceServer ! ASearchRows(searchTemp.get.map(ASearchRow(None, LocalDateTime.now().withMillisOfSecond(0), nid.toString, _)))
              if (temp.base.pname.isDefined) {
                persistanceServer ! NewsPublisherRow(None, LocalDateTime.now().withMillisOfSecond(0), temp.base.pname.get, temp.base.picon, temp.base.pdescr, concern = 0)
              }
              shutdown()
            case None => shutdown()
          }
        }
      }

      // TODO: title -> ners -> douban,baike
      // TODO: title -> tags -> zhihu
      // TODO: content -> compress

      def shutdown() = {
        context.stop(self)
      }

      override def postStop() = {
        timer.cancel()
      }
    }))
  }
}

object SpiderNewsPipelineServer {

  case object ChcekReadyInTime

  def props(persistanceServer: ActorRef, imageServer: ActorRef)(implicit mat: Materializer): Props =
    Props(new SpiderNewsPipelineServer(persistanceServer, imageServer))

  def verifyNewsTemp(task: String): Future[Either[String, NewsTemp]] = {
    cache.hgetall[String](task).map {
      case cache: Map[String, String] if cache.isEmpty => Left(s"CacheEmptyErr: $task")
      case cache: Map[String, String] =>
        try {
          Right(NewsTemp(
            base = BaseTemp(
              url = cache.get("url").get,
              title = cache.get("title").get,
              tags = cache.get("keywords"),
              author = cache.get("author"),
              ptime = dateTimeStr2DateTime(cache.get("pub_time").get),
              pname = cache.get("pub_name"),
              purl = cache.get("pub_url"),
              picon = cache.get("pub_icon"),
              pdescr = cache.get("pub_descr"),
              html = cache.get("content_html").get,
              synopsis = cache.get("synopsis"),
              province = cache.get("province"),
              city = cache.get("city"),
              district = cache.get("district"),
              docid = cache.get("docid").get,
              content = Json.parse(cache.get("content").get).as[List[NewsBodyBlock]]
            ),
            syst = SystTemp(
              chid = cache.get("channel_id").get.toLong,
              sechid = cache.get("se_channel_id").map(_.toLong),
              srid = cache.get("source_id").get.toLong,
              srstate = cache.get("source_online").get.toInt,
              pconf = cache.get("task_conf").map { conf => Json.parse(conf).as[JsValue] },
              comment_queue = cache.get("comment_queue"),
              comment_task = cache.get("comment_task")
            )))
        } catch {
          case NonFatal(e) => Left(s"CacheVerifyErr: ${e.getMessage}, $task")
        }
    }.recover {
      case NonFatal(e) => Left(s"CacheVerifyErr: ${e.getMessage}, $task")
    }
  }

  def formatNewsRow(newsTemp: NewsTemp, feedOSSTemp: Option[List[String]], detailOSSTemp: Option[List[Oss]]): Option[NewsRow] = {
    try {
      val (baseTemp: BaseTemp, systTemp: SystTemp) = (newsTemp.base, newsTemp.syst)
      val imageNumber: Int = baseTemp.content.collect { case ImageBlock(_) => 1 }.sum

      val content: JsValue = {
        val ossTemp: Map[String, String] = if (detailOSSTemp.isDefined) {
          detailOSSTemp.get.map { case Oss(ori, oss) => ori -> oss }.toMap[String, String]
        } else Map.empty
        val blocks: List[NewsBodyBlock] = baseTemp.content.collect {
          case ImageBlock(src) if ossTemp.get(src).isDefined =>
            ImageBlock(ossTemp(src))
          case block @ TextBlock(_)  => block
          case block @ VideoBlock(_) => block
        }
        Json.toJson(blocks)
      }

      val (style: Int, imgs: Option[List[String]]) = feedOSSTemp match {
        case Some(osss) if osss.size >= 3 => (3, Some(osss.slice(0, 3)))
        case Some(osss) if osss.size == 2 => (2, Some(osss))
        case Some(osss) if osss.size == 1 => (1, Some(osss))
        case _                            => (0, None)
      }

      val base: NewsRowBase = NewsRowBase(
        nid = None,
        url = baseTemp.url,
        docid = baseTemp.docid,
        title = baseTemp.title,
        content = content,
        html = baseTemp.html,
        author = baseTemp.author,
        ptime = baseTemp.ptime,
        pname = baseTemp.pname,
        purl = baseTemp.purl,
        descr = baseTemp.synopsis,
        tags = if (baseTemp.tags.isDefined && baseTemp.tags.nonEmpty) Some(baseTemp.tags.get.trim.split(",").toList.filter(_.nonEmpty).map(_.trim)) else None,
        province = baseTemp.province,
        city = baseTemp.city,
        district = baseTemp.district)

      val incr: NewsRowIncr = NewsRowIncr(collect = 0, concern = 0, comment = 0, inum = imageNumber, style = style, imgs = imgs, compress = None, ners = None)

      val syst: NewsRowSyst = NewsRowSyst(state = 0,
        ctime = LocalDateTime.now().withMillisOfSecond(0),
        chid = systTemp.chid,
        sechid = systTemp.sechid,
        srid = systTemp.srid, srstate = systTemp.srstate,
        pconf = None,
        plog = None)

      Some(NewsRow(base, incr, syst))
    } catch {
      case NonFatal(e) =>
        println(s"SpiderNewsPipelineServer.formatNewsRow(${newsTemp.base.url}): ${e.getMessage}")
        None
    }
  }

  private final val makeLock: (String => String) = (key: String) => s"${md5Hash(key, random = false)}:NULK"
  private final val PROCE_LOCK: Int = 0
  private final val CACHE_LOCK: Int = 1
  private final val CACHE_LIMITED: Long = 60L * 60 * 24 * 30 // 1 month

  def verifyNewsUnique(url: String, title: String, docid: String): Future[Boolean] = {
    getUniqueLock(url, title, docid).map {
      case (None, None, None)                                                       => true
      case (uOpt, tOpt, dOpt) if uOpt.isDefined || tOpt.isDefined || dOpt.isDefined => false
    }
  }

  def setUniqueLock(url: String, title: String, docid: String, lock: Int, expires: Long): Unit = {
    cache.setex(makeLock(url), expires, lock).recover { case NonFatal(e) => false }
    cache.setex(makeLock(title), expires, lock).recover { case NonFatal(e) => false }
    cache.setex(makeLock(docid), expires, lock).recover { case NonFatal(e) => false }
  }

  def getUniqueLock(url: String, title: String, docid: String): Future[(Option[Int], Option[Int], Option[Int])] = {
    cache.mget[String](makeLock(url), makeLock(title), makeLock(docid)).map {
      case Seq(u, t, d, _*) => (u.map(_.toInt), t.map(_.toInt), d.map(_.toInt))
    }.recover {
      case NonFatal(e) => (None, None, None)
    }
  }
}