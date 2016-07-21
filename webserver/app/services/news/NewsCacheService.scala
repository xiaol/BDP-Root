package services.news

import java.text.SimpleDateFormat
import java.util.{ Calendar, Date }
import javax.inject.Inject

import akka.util.ByteString
import com.google.inject.ImplementedBy
import commons.models.news.{ NewsFeedResponse, NewsRow }
import dao.news.NewsDAO
import play.api.Logger
import play.api.libs.json.Json
import utils.RedisDriver.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.parsing.json.{ JSON, JSONObject }
import scala.collection.Set

/**
 * Created by zhangshl on 2016-07-04.
 *
 */

@ImplementedBy(classOf[NewsCacheService])
trait INewsCacheService {
  def getNewRowCache(nid: Long): Future[Option[NewsRow]]
  def addRecommendNew(nid: Long, score: Double): Future[Boolean]
  def findNewsRowByNid(nid: Long): Future[NewsRow]
  def setNewRowCache(newRow: NewsRow, score: Double): Future[Boolean]
  def addNewRecommendset(nid: Long, score: Double): Future[Long]
  def removeNewRecommendset(remnidreq: Seq[String]): Future[Long]
  def getNewRowList(uid: Long, page: Int): Future[Seq[NewsFeedResponse]]
  def getNewsRecommendReadSeq(uid: Long): Future[Seq[String]]
  def getRecommendNidSeq(newsrecommendreadset: Set[String], page: Int): Future[Seq[String]]
  def addNewsRecommendReadSet(uid: Long, recommendlist: Seq[String]): Future[Long]
  def getNewRowCacheList(recommendNidSeq: Seq[String]): Future[Seq[NewsFeedResponse]]
}

class NewsCacheService @Inject() (val newsDAO: NewsDAO) extends INewsCacheService {

  //新闻后缀
  private val rowSuffix = ":nrow"
  //用户已浏览列表后缀
  private val userReadSuffix = ":uread"
  private val newRowName: (Long => String) = (nid: Long) => s"${nid.toString}$rowSuffix"
  private val userReadListName: (Long => String) = (uid: Long) => s"${uid.toString}$userReadSuffix"
  private val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  //根据nid获取新闻对象
  def getNewRowCache(nid: Long): Future[Option[NewsRow]] = {
    cache.get[String](newRowName(nid)).map {
      case Some(newJson) => Some(Json.parse(newJson).as[NewsRow])
      case _             => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewRowCache($nid): ${e.getMessage}")
        None
    }
  }

  //将新闻设为推荐新闻
  def addRecommendNew(nid: Long, score: Double): Future[Boolean] = {
    val newRow = findNewsRowByNid(nid)
    //删除过期的推荐nid列表
    val remnids = getRemNidSeq()
    for {
      newRow <- newRow
      result <- setNewRowCache(newRow, score)
      //存新闻的同时,插入推荐列表
      add <- addNewRecommendset(newRow.base.nid.get, score)
      remnidreq <- remnids
      r <- removeNewRecommendset(remnidreq)
    } yield result
  }

  //根据nid查询新闻内容
  def findNewsRowByNid(nid: Long): Future[NewsRow] = {
    newsDAO.findByNid(nid).map { news =>
      news match {
        case Some(newsRow: NewsRow) => newsRow
      }
    }
  }

  //将新闻对象存入缓存,保存两天
  def setNewRowCache(newRow: NewsRow, score: Double): Future[Boolean] = {
    cache.set[String](newRowName(newRow.base.nid.get), Json.toJson(newRow).toString, Some(60 * 60 * 24 * 7L)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.setNewRowCache($newRow): ${e.getMessage}")
        false
    }
  }

  //将推荐新闻nid加入推荐列表SortedSet
  //'{"nid":"3214221","rtime":"2016-07-06 12:22:48"}'
  def addNewRecommendset(nid: Long, score: Double): Future[Long] = {
    val map = Map("nid" -> nid.toString, "rtime" -> dateFormat.format(new Date()))
    cache.zadd("newsrecommendsortedset", (score, JSONObject(map).toString())).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.addNewRecommendset($nid): ${e.getMessage}")
        0L
    }
  }

  //获取过期的nid列表
  def getRemNidSeq(): Future[Seq[String]] = {
    val mresult: Future[Seq[String]] = cache.zrevrange("newsrecommendsortedset", 0, -1).map { seq =>
      seq.map {
        obj => obj.utf8String
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewRowList.zrevrange1(): ${e.getMessage}")
        Seq[String]()
    }
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DATE, -2)
    val remnids = mresult.map { e: Seq[String] =>
      e.filter { (r: String) =>
        var rtime = dateFormat.parse(r.split(",")(1).toString.split("\"")(3))
        rtime.before(cal.getTime)
      }
    }
    remnids
  }

  //将过期的新闻数据从推荐列表中删除
  //'{"nid":"3214221","rtime":"2016-07-06 12:22:48"}'
  //分成两个方法
  def removeNewRecommendset(remnidreq: Seq[String]): Future[Long] = {
    println("remnidreq==" + remnidreq)
    cache.zrem("newsrecommendsortedset", remnidreq: _*).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.removeNewRecommendset(zrevrange): ${e.getMessage}")
        0L
    }
  }

  //用户获取推荐新闻nid列表
  def getNewRowList(uid: Long, page: Int): Future[Seq[NewsFeedResponse]] = {
    for {
      //步骤一:获取已浏览列表
      newsrecommendreadset <- getNewsRecommendReadSeq(uid)
      //步骤二:获取推荐列表,去除已浏览
      recommendNidSeq <- getRecommendNidSeq(newsrecommendreadset.toSet, page: Int)
      //步骤三:将获取推荐列表加入已浏览
      add <- addNewsRecommendReadSet(uid, recommendNidSeq)
      //步骤四:遍历nid列表,查询对应新闻
      newsFeedResponses <- getNewRowCacheList(recommendNidSeq)

    } yield newsFeedResponses
  }

  //步骤一:获取已浏览列表
  def getNewsRecommendReadSeq(uid: Long): Future[Seq[String]] = {
    cache.smembers(userReadListName(uid)).map { seq =>
      seq.map {
        nid =>
          nid.utf8String
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.getNewsRecommendReadSeq($uid): ${e.getMessage}")
        Seq[String]()
    }
  }

  //步骤二:获取推荐列表,去除已浏览
  def getRecommendNidSeq(newsrecommendreadset: Set[String], page: Int): Future[Seq[String]] = {
    //若已浏览列表为空,则直接取前10条,若不为空,则去重已浏览列表
    if (!newsrecommendreadset.isEmpty) {
      //用户已浏览列表中存在数据,则与推荐列表比较,已浏览的不再推荐
      val mresult: Future[Seq[String]] = cache.zrevrange("newsrecommendsortedset", 0, -1).map { seq =>
        seq.map {
          obj =>
            //'{"nid":"3214221","rtime":"2016-07-06 12:22:48"}'
            val s: String = obj.utf8String.split(",")(0).toString.split(":")(1).trim.replaceAll("\"", "")
            s.toString
        }
      }.recover {
        case NonFatal(e) =>
          Logger.error(s"Within NewsCacheService.getNewRowList.zrevrange1(): ${e.getMessage}")
          Seq[String]()
      }

      mresult.map { e: Seq[String] =>
        e.filter((r: String) => !newsrecommendreadset.contains(r.toString)).take(page)
      }
    } else {
      //直接推荐前10个
      val mresult = cache.zrevrange("newsrecommendsortedset", 0, page - 1).map { seq =>
        seq.map {
          obj =>
            obj.utf8String.split(",")(0).toString.split(":")(1).trim.replaceAll("\"", "")
          //            var rmap = JSON.parseFull(obj.utf8String)
          //            rmap match {
          //              case Some(cmap: Map[String, Any]) => cmap.get("nid")
          //              case _                            => None
          //            }
        }
      }.recover {
        case NonFatal(e) =>
          Logger.error(s"Within NewsCacheService.getNewRowList.zrevrange2(): ${e.getMessage}")
          Seq[String]()
      }
      mresult

      //      val result = mresult.map { x =>
      //        x.map { y =>
      //          y match {
      //            case Some(nid: String) => nid
      //          }
      //        }
      //      }
      //      result
    }
  }

  //步骤三:将获取推荐列表加入已浏览
  def addNewsRecommendReadSet(uid: Long, recommendlist: Seq[String]): Future[Long] = {
    cache.sadd(userReadListName(uid), recommendlist: _*).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsCacheService.addNewsRecommendReadSet.sadd($uid):(${recommendlist.toString()}): ${e.getMessage}")
        0L
    }
  }

  //步骤四:遍历nid列表,查询对应新闻
  def getNewRowCacheList(recommendNidSeq: Seq[String]): Future[Seq[NewsFeedResponse]] = {
    if (recommendNidSeq.length > 0) {
      val arr = recommendNidSeq.map { x => newRowName(x.toLong) }
      val seq = cache.mget(arr: _*).map { x: Seq[Option[ByteString]] =>
        x.map {
          case Some(newJson) => Some(Json.parse(newJson.utf8String).as[NewsRow])
          case _             => None
        }
      }

      val result = seq.map { x =>
        x.map { y =>
          y match {
            case Some(newsRow: NewsRow) =>
              NewsFeedResponse.from(newsRow)
          }
        }
      }
      result
    } else {
      Future.successful(Seq[NewsFeedResponse]())
    }

  }

}