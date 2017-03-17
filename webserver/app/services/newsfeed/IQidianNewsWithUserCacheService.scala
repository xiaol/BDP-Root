package services.newsfeed

import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.advertisement.AdRequest
import commons.models.news._
import commons.utils.JodaOderingImplicits
import commons.utils.JodaOderingImplicits.LocalDateTimeReverseOrdering
import commons.utils.JodaUtils._
import dao.news._
import dao.newsfeed.NewsFeedDao
import dao.userprofiles.HateNewsDAO
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json.Json
import services.advertisement.AdResponseService
import services.news.NewsCacheService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-09.
 *
 */

@ImplementedBy(classOf[QidianNewsWithUserCacheService])
trait IQidianNewsWithUserCacheService {
  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]]
  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]]
}

class QidianNewsWithUserCacheService @Inject() (val newsUnionFeedDao: NewsUnionFeedDao, val newsResponseDao: NewsResponseDao, val topicListDAO: TopicListDAO, val adResponseService: AdResponseService,
                                                val newsFeedDao: NewsFeedDao, val newsRecommendReadDAO: NewsRecommendReadDAO, val topicNewsReadDAO: TopicNewsReadDAO, val hateNewsDAO: HateNewsDAO)
    extends IQidianNewsWithUserCacheService with NewsCacheService {

  def updateNewsFeedCommon(): Future[Boolean] = {
    Future {
      newsUnionFeedDao.commonAll(500).map { seqall =>
        val data = seqall.map { news =>
          val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
          newsFeedResponse
        }
        remNewsFeedCommonSetCache().flatMap {
          case 1 => setNewsFeedCommonSetCache(data.map { news => Json.toJson(news).toString })
          case _ => Future.successful(0L)
        }
      }
    }
    Future.successful(true)
  }

  def getFeedData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[(Seq[NewsFeedResponse], Int)] = {
    {
      //从缓存取数据
      val cachefeed = getNewsFeedCache(uid)

      val result = cachefeed.flatMap {
        //缓存中有数据情况下, 获取需要的数据, 标识1: 需要更新用户缓存
        case Some(news: Seq[NewsFeedResponse]) if news.length >= 5 => Future.successful((news, 1))
        case _ =>
          //缓存中没有该用户数据, 修改用户状态
          getUserStateCache(uid).map { state =>
            state match {
              //无状态位, 设置状态位, 有状态位,说明已经在准备数据
              case None => setUserStateCache(uid, "0")
              case _    =>
            }
          }

          val rcache = getNewsFeedCommonSetCache(count).flatMap { seq =>
            seq match {
              case Some(news: Seq[NewsFeedResponse]) if news.length >= 5 => Future.successful(news, 0)
              case _ =>
                //缓存所有用户共同新闻
                Future {
                  newsUnionFeedDao.commonAll(500).map { seqall =>
                    val data = seqall.map { news =>
                      val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
                      newsFeedResponse
                    }
                    setNewsFeedCommonSetCache(data.map { news => Json.toJson(news).toString })
                  }
                }
                //只取点击量高和普通新闻
                getCommonData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]).map { seq => (seq, 0) }
            }
          }
          rcache
      }
      result
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.getFeedData($uid, $page, $count, $timeCursor, $adbody, $t, $remoteAddress, $v): ${e.getMessage}")
        (Seq[NewsFeedResponse](), 0)
    }
  }

  def setData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Boolean] = {
    {
      //等待1秒以上, 由于是从 从库获取数据,浏览记录需要从主库同步到从库需要一定的时间
      Thread.sleep(Random.nextInt(3))
      val result: Future[Seq[NewsFeedResponse]] = getData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String])
      result.flatMap { data =>
        data match {
          case data: Seq[NewsFeedResponse] if data.nonEmpty && data.length > 0 =>
            val flag = setNewsFeedCache(uid, data.take(count.toInt * 7))
            //删除用户状态位
            remUserStateCache(uid)
            flag
          case _ => Future.successful(false)
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.setData($uid, $page, $count, $timeCursor, $adbody, $t, $remoteAddress, $v): ${e.getMessage}")
        false
    }
  }

  def getData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      //新闻
      val newsFO: Future[Seq[NewsFeedResponse]] = qidian(uid, page, count, timeCursor, t, v)
      //      //广告
      //      val adFO: Future[Seq[NewsFeedResponse]] = adbody match {
      //        case Some(body: String) =>
      //          val request: AdRequest = Json.parse(body).as[AdRequest]
      //          val impression = request.impression.map { imp => imp.copy(page_size = Some("10")) }
      //          adResponseService.getAdResponse(Json.toJson(request.copy(impression = impression)).toString(), remoteAddress, uid)
      //        case _ => Future.successful(Seq[NewsFeedResponse]())
      //      }

      //      val result: Future[Seq[NewsFeedResponse]] = for {
      //        news <- newsFO
      //        ad <- adFO
      //      } yield {
      //        (ad ++: news)
      //      }
      newsFO.map { feed =>
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.getData($uid, $page, $count, $timeCursor, $adbody, $t, $remoteAddress, $v): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def getCommonData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      //新闻
      val refreshCommonFO = newsUnionFeedDao.common((page - 1) * count, count, createTimeCursor4Refresh(timeCursor), uid)
      //不感兴趣新闻,获取来源和频道
      val hateNews: Future[Seq[NewsRow]] = hateNewsDAO.getNewsByUid(uid)

      val result: Future[Seq[NewsFeedResponse]] = for {
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }
        hatePnameWithChid <- hateNews
      } yield {
        (commons).filter { feed =>
          var flag = true
          hatePnameWithChid.foreach { news =>
            if (news.base.pname.getOrElse("1").equals(feed.pname.getOrElse("2"))) {
              flag = false
            }
          }
          flag
        }.take(count.toInt + 2)
      }
      result.map { feed =>
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.getData($uid, $page, $count, $timeCursor, $adbody, $t, $remoteAddress, $v): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  private def qidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      val times = 5
      val level1 = count / 2 * times
      val level2 = count / 4 * times
      val level3 = count / 4 * times
      val level4 = count / 9 * times

      //----热点部分----
      //百度热词 和 有评论
      val refreshHotFO = newsUnionFeedDao.hot((page - 1) * count, level3, newTimeCursor, uid)

      //----推荐部分----
      //模型LDA 和 Kmeans推荐
      val refreshLDAandKmeansFO = newsUnionFeedDao.byLDAandKmeans((page - 1) * count, level1, newTimeCursor, uid)

      //根据用户偏好从人工选取 和 人工推荐(没有偏好数据时使用)
      val refreshByPeopleRecommendWithClickFO = newsUnionFeedDao.byPeopleRecommendWithClick((page - 1) * count, level2, newTimeCursor, uid)

      //-------补全新闻------补全流程:(LDA + Kmeans)没有时, 出点击量高的新闻 --> 普通新闻
      val refreshCommonFO = newsUnionFeedDao.common((page - 1) * count, count * times, newTimeCursor, uid)

      //----大图和视频部分----
      //大图新闻 和 视频
      val refreshBigImageAndVideo = v match {
        case Some(video: Int) if video == 1 => newsUnionFeedDao.byBigImageAndVideo((page - 1) * count, level4, newTimeCursor, uid)
        case _                              => newsUnionFeedDao.byBigImage((page - 1) * count, level4, newTimeCursor, uid)
      }

      //专题
      val topicsFO: Future[Seq[TopicList]] = t match {
        case 1 => topicListDAO.topicShow(uid).map(_.take(level4.toInt))
        case _ => Future.successful { Seq[TopicList]() }
      }

      //不感兴趣新闻,获取来源和频道
      val hateNews: Future[Seq[NewsRow]] = hateNewsDAO.getNewsByUid(uid)

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val result: Future[Seq[NewsFeedResponse]] = for {
        hots <- refreshHotFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }

        lDAandKmeans <- refreshLDAandKmeansFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }

        peopleRecommendWithClick <- refreshByPeopleRecommendWithClickFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }
        commons <- refreshCommonFO.map { seq =>
          seq.map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }
        bigimg5 <- refreshBigImageAndVideo.map { seq =>
          seq.filter(_._20 == Some(999)).map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }

        video <- refreshBigImageAndVideo.map { seq =>
          seq.filter(_._20 == Some(6)).take(v.getOrElse(0)).map { news =>
            val newsFeedResponse: NewsFeedResponse = toNewsFeedResponse(news._1, news._2, news._3, news._4, news._5, news._6, news._7, news._8, news._9, news._10, news._11, news._12, news._13, news._14, news._15, news._16, news._17, news._18, news._19, news._20, news._21)
            newsFeedResponse
          }
        }

        topics <- topicsFO.map {
          case seq: Seq[TopicList] => seq.map { topic =>
            NewsFeedResponse.from(topic)
          }
        }

        hatePnameWithChid <- hateNews

      } yield {
        ((bigimg5 ++: topics ++: lDAandKmeans.take(level1.toInt) ++: hots.take(level3.toInt) ++: video ++: peopleRecommendWithClick.take(level2.toInt)).take((count.toInt - 1) * 5)
          ++: commons).filter { feed =>
            var flag = true
            hatePnameWithChid.foreach { news =>
              if (news.base.pname.getOrElse("1").equals(feed.pname.getOrElse("2"))) {
                flag = false
              }
            }
            flag
          }.take((count.toInt - 1) * 7)
      }

      result

    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.qidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  private def getReturnData(count: Long, t: Int, v: Option[Int], alldata: Seq[NewsFeedResponse]): Seq[NewsFeedResponse] = {
    val level1 = count / 2
    val level2 = count / 4
    val level3 = count / 4
    val level4 = count / 9

    //----热点部分----
    //百度热词 和 有评论
    val refreshHotFO = alldata.filter(_.rtype.getOrElse(0) == 1).take(level2.toInt)

    //----推荐部分----
    //模型LDA 和 Kmeans推荐
    val refreshLDAandKmeansFO = alldata.filter(_.rtype.getOrElse(0) == 21).take(level1.toInt).map(news => news.copy(rtype = Some(0)))

    //根据用户偏好从人工选取 和 人工推荐(没有偏好数据时使用)
    val refreshByPeopleRecommendWithClickFO = alldata.filter(_.rtype.getOrElse(0) == 2).take(level2.toInt)

    //-------补全新闻------补全流程:(LDA + Kmeans)没有时, 出点击量高的新闻 --> 普通新闻
    val refreshCommonFO = alldata.filter(_.rtype.getOrElse(0) == 0).take(count.toInt)

    //----大图和视频部分----
    //大图新闻 和 视频
    val refreshBigImageAndVideo = v match {
      case Some(video: Int) if video == 1 => alldata.filter(_.style > 10).take(level4.toInt) ++: alldata.filter(_.rtype.getOrElse(0) == 6).take(level4.toInt)
      case _                              => alldata.filter(_.style > 10).take(level4.toInt)
    }
    //专题
    val topicsFO = alldata.filter(_.rtype.getOrElse(0) == 4).take(level4.toInt)

    val adFO = alldata.filter(_.rtype.getOrElse(0) == 3).take(level4.toInt)

    (adFO ++: refreshBigImageAndVideo ++: topicsFO ++: refreshLDAandKmeansFO ++: refreshHotFO ++: refreshByPeopleRecommendWithClickFO ++: refreshCommonFO)
  }

  private def updateCacheData(uid: Long, read: Future[Seq[NewsFeedResponse]], alldata: Future[Seq[NewsFeedResponse]]): Future[Boolean] = {
    {
      val result: Future[Seq[NewsFeedResponse]] = for {
        r <- read
        a <- alldata
      } yield {
        a.filter { feed =>
          var flag = true
          r.foreach { news =>
            if (news.nid.equals(feed.nid) && feed.rtype.getOrElse(0) != 41) {
              flag = false
            }
          }
          flag
        }
      }
      //判断剩下的普通新闻条数,如果少于5条,就重新准备数据
      result.map { seq =>
        seq.filter(_.rtype.getOrElse(0) == 0).size match {
          case length: Int if length < 5 => getUserStateCache(uid).map { state =>
            state match {
              //无状态位, 设置状态位, 有状态位,说明已经在准备数据
              case None => setUserStateCache(uid, "0")
              case _    =>
            }
          }
          case _ =>
        }
      }

      result.flatMap { news => setNewsFeedCache(uid, news) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.updateCacheData(): ${e.getMessage}")
        false
    }
  }

  def refreshQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = createTimeCursor4Refresh(timeCursor)

      //rtype类型:0普通、1热点、2推送(2人工推送, 21机器推送)、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[(Seq[NewsFeedResponse], Int)] = getFeedData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String])
      val returnData: Future[Seq[NewsFeedResponse]] = r.map { seq => getReturnData(count: Long, t: Int, v: Option[Int], seq._1: Seq[NewsFeedResponse]) }

      //广告
      val adFO: Future[Seq[NewsFeedResponse]] = adbody match {
        case Some(body: String) => adResponseService.getAdResponse(body, remoteAddress, uid)
        case _                  => Future.successful(Seq[NewsFeedResponse]())
      }

      val returnDataWithAD: Future[Seq[NewsFeedResponse]] = for {
        news <- returnData
        ad <- adFO
      } yield {
        (ad ++: news).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = returnDataWithAD.map { seq =>

        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内,同一来源时间不能一样,否则后端排好序,APP端重排序时可能排在一起
        var nums: Map[String, LocalDateTime] = Map()
        seq.map { n =>
          if (n.rtype.getOrElse(0) == 41) {
            //专题置顶
            n.copy(ptime = newTimeCursor.plusSeconds(10)).copy(rtype = Some(4))
          } else if (n.rtype.getOrElse(0) == 999 && n.style > 10) {
            //5等级大图新闻
            n.copy(ptime = newTimeCursor.plusSeconds(9))
          } else if (n.rtype.getOrElse(0) == 999) {
            //5等级非大图
            n.copy(ptime = newTimeCursor.plusSeconds(8))
          } else if (n.rtype.getOrElse(0) == 3) {
            //广告
            n.copy(ptime = newTimeCursor.plusSeconds(6))
          } else {
            n.copy(ptime = newTimeCursor.plusSeconds(Random.nextInt(6)))
          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        val feeds = seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
        //-----------将广告放在第六个-----------
        //将广告放在第6个
        var seccount = 10
        val newsfeed = feeds.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor)
      }.map(_.take(count.toInt))

      //更新缓存, 去除已读列表
      Future {
        r.map { re =>
          re._2 match {
            case s: Int if s == 1 => updateCacheData(uid: Long, result: Future[Seq[NewsFeedResponse]], r.map(_._1): Future[Seq[NewsFeedResponse]])
            case _                =>
          }
        }
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), v.logtype, Some(1)) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }

      //将99的改回
      result.map { seq =>
        val feed = seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //最后再设置下一次缓存, 避免本次新闻尚未插入已浏览记录表, 下一次取到重复新闻
          Future {
            getUserStateCache(uid).map { state =>
              state match {
                case Some(state: String) if state == "0" =>
                  //准备数据中状态
                  setUserStateCache(uid, "1")
                  //准备数据
                  setData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String])
                case _ =>
              }
            }
          }

          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.refreshQidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def loadQidian(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String]): Future[Seq[NewsFeedResponse]] = {
    {
      val newTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)

      //rtype类型:0普通、1热点、2推送、3广告、4专题、5图片新闻、6视频、7本地
      val r: Future[(Seq[NewsFeedResponse], Int)] = getFeedData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String])
      val returnData: Future[Seq[NewsFeedResponse]] = r.map { seq => getReturnData(count: Long, t: Int, v: Option[Int], seq._1: Seq[NewsFeedResponse]) }
      //广告
      val adFO: Future[Seq[NewsFeedResponse]] = adbody match {
        case Some(body: String) => adResponseService.getAdResponse(body, remoteAddress, uid)
        case _                  => Future.successful(Seq[NewsFeedResponse]())
      }

      val returnDataWithAD: Future[Seq[NewsFeedResponse]] = for {
        news <- returnData
        ad <- adFO
      } yield {
        (ad ++: news).take(count.toInt + 2)
      }
      //规则一:去重复新闻,一个来源可能重复
      //规则二:重做时间
      //规则三:过滤连续重复来源
      val result: Future[Seq[NewsFeedResponse]] = returnDataWithAD.map { seq =>

        val date = new Date(timeCursor)
        val localDateTime = LocalDateTime.fromDateFields(date)
        var flag = true
        //将推荐新闻、普通新闻时间,伪造为热点时间前后3秒内
        seq.sortBy(_.ptime).map { n =>
          if (n.rtype.getOrElse(0) == 999) {
            flag = false
            n.copy(ptime = localDateTime.plusSeconds(-1))
          } else if (n.rtype.getOrElse(0) == 4) {
            flag = false
            n.copy(ptime = localDateTime.plusSeconds(-2))
          } else if (n.rtype.getOrElse(0) == 3) {
            n.copy(ptime = localDateTime.plusSeconds(-3))
          } else {
            if (flag) {
              flag = false
              n.copy(ptime = localDateTime.plusSeconds(-2))
            } else {
              var newtime = localDateTime.plusSeconds(Random.nextInt(10) - 15)
              n.copy(ptime = newtime)
            }

          }
        }
      }.map { seq =>
        var nids = seq.map { n => (n.nid, 1) }.toMap
        var pname = ""

        //去除组合中的重复新闻,去除连续出现同一来源(要放在改时间之后,要不然有可能再排序到一起)
        val feeds = seq.sortBy(_.ptime).filter { n =>
          if (nids.contains(n.nid) && nids.get(n.nid).getOrElse(1) == 1 && !pname.equals(n.pname.get)) {
            nids += (n.nid -> 2)
            pname = n.pname.getOrElse("")
            true
          } else {
            false
          }
        }
        //-----------将广告放在第六个-----------
        //将广告放在第6个
        var seccount = 0
        val newsfeed = feeds.map { news =>
          seccount = seccount - 1
          news.copy(ptime = newTimeCursor.plusSeconds(seccount))
        }
        changeADtime(newsfeed, newTimeCursor)
      }.map(_.take(count.toInt))

      //更新缓存, 去除已读列表
      Future {
        r.map { re =>
          re._2 match {
            case s: Int if s == 1 => updateCacheData(uid: Long, result: Future[Seq[NewsFeedResponse]], r.map(_._1): Future[Seq[NewsFeedResponse]])
            case _                =>
          }
        }
      }

      val newsRecommendReads: Future[Seq[NewsRecommendRead]] = result.map { seq => seq.filter(_.rtype.getOrElse(0) != 3).filter(_.rtype.getOrElse(0) != 4).map { v => NewsRecommendRead(uid, v.nid, LocalDateTime.now(), v.logtype, Some(1)) } }
      //从结果中取要浏览的20条,插入已浏览表中
      newsRecommendReads.map { seq => newsRecommendReadDAO.insert(seq) }
      newsRecommendReads.map { seq => newsFeedDao.insertRead(seq) }
      //专题
      result.map { seq => seq.filter(_.rtype.getOrElse(0) == 4).map { v => topicNewsReadDAO.insertByTid(uid, v.nid.toInt) } }
      //将99的改回
      result.map { seq =>
        val feed = seq.map { r: NewsFeedResponse =>
          if (r.rtype.getOrElse(0) == 999)
            r.copy(rtype = Some(2))
          else
            r
        }
        //若只有广告,返回空
        if (feed.filter(_.rtype.getOrElse(0) != 3).length > 0) {
          //最后再设置下一次缓存, 避免本次新闻尚未插入已浏览记录表, 下一次取到重复新闻
          Future {
            getUserStateCache(uid).map { state =>
              state match {
                case Some(state: String) if state == "0" =>
                  //准备数据中状态
                  setUserStateCache(uid, "1")
                  //准备数据
                  setData(uid: Long, page: Long, count: Long, timeCursor: Long, t: Int, v: Option[Int], adbody: Option[String], remoteAddress: Option[String])
                case _ =>
              }
            }
          }
          feed
        } else {
          Seq[NewsFeedResponse]()
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within QidianNewsWithUserCacheService.loadQidian($timeCursor): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def toNewsFeedResponse(nid: Long, url: String, docid: String, title: String, pname: Option[String], purl: Option[String],
                         collect: Int, concern: Int, comment: Int, inum: Int, style: Int, imgs: Option[String], state: Int,
                         ctime: Timestamp, chid: Long, icon: Option[String], videourl: Option[String], thumbnail: Option[String],
                         duration: Option[Int], rtype: Option[Int], logtype: Option[Int]): NewsFeedResponse = {
    val imgsList = imgs match {
      case Some(str) =>
        Some(str.split(",").toList)
      case _ => None
    }

    val date = new Date(ctime.getTime)
    val newsSimpleRowBase = NewsSimpleRowBase(Some(nid), url, docid, title, None, LocalDateTime.fromDateFields(date), pname, purl, None, None)
    val newsSimpleRowIncr = NewsSimpleRowIncr(collect, concern, comment, inum, style, imgsList)
    val newsSimpleRowSyst = NewsSimpleRowSyst(state, LocalDateTime.fromDateFields(date), chid, None, icon, rtype, videourl, thumbnail, duration, logtype, Some(1))
    val newsSimpleRow = NewsSimpleRow(newsSimpleRowBase, newsSimpleRowIncr, newsSimpleRowSyst)
    NewsFeedResponse.from(newsSimpleRow)
  }

  final private def createTimeCursor4Refresh(timeCursor: Long): LocalDateTime = {
    val reqTimeCursor: LocalDateTime = msecondsToDatetime(timeCursor)
    val oldTimeCursor: LocalDateTime = dateTimeStr2DateTime(getDatetimeNow(-12))
    if (reqTimeCursor.isBefore(oldTimeCursor)) oldTimeCursor else reqTimeCursor
  }

  def changeADtime(feeds: Seq[NewsFeedResponse], newTimeCursor: LocalDateTime): Seq[NewsFeedResponse] = {
    //-----------将广告放在第六个-----------
    if (feeds.filter(_.rtype == Some(3)).length > 0) {
      //第六个新闻nid和时间
      val nid6 = feeds.take(6).lastOption match {
        case Some(news) => news.nid
        case _          => 0L
      }
      val time6 = feeds.take(6).lastOption match {
        case Some(news) => news.ptime
        case _          => newTimeCursor
      }
      //广告的时间
      val timead = feeds.filter(_.rtype == Some(3)).headOption match {
        case Some(news) => news.ptime
        case _          => newTimeCursor
      }
      //广告时间和第六条新闻时间互换
      feeds.map { news =>
        if (news.rtype == Some(3)) {
          news.copy(ptime = time6)
        } else if (news.nid == nid6) {
          news.copy(ptime = timead)
        } else {
          news
        }
      }.sortBy(_.ptime)
    } else {
      feeds
    }
  }

}
