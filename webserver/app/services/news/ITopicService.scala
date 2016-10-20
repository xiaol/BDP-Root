package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news._
import dao.news.{ TopicNewsDAO, TopicClassListDao, TopicListDAO }
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangsl on 2016-10-17.
 *
 */

@ImplementedBy(classOf[TopicService])
trait ITopicService {
  def topicDetail(tid: Int): Future[Option[TopicRelationClass]]
}

class TopicService @Inject() (val topicListDAO: TopicListDAO, val topicClassListDao: TopicClassListDao, val topicNewsDAO: TopicNewsDAO) extends ITopicService {

  def topicDetail(tid: Int): Future[Option[TopicRelationClass]] = {
    topicListDAO.topicDetail(tid).map { seq =>
      val topic = seq.map(_._1).head
      val topic_class_list = seq.map(_._2).distinct
      val topic_news_list = seq.map(_._3)
      val newsrow_list = seq.map(_._4)

      val topicClassRelationNews = topic_class_list.map { topic_class =>
        val nids = topic_news_list.filter(_.topic_class == topic_class.id).map(_.news.toLong)
        val newsFeedResponses = newsrow_list.filter { newsrow =>
          var flag = false
          nids.foreach { nid =>
            if (nid == newsrow.base.nid.getOrElse(0)) {
              flag = true
            }
          }
          flag
        }.map { newsrow => NewsFeedResponse.from(newsrow) }

        TopicClassRelationNews(topic_class, newsFeedResponses.toList)
      }
      Some(TopicRelationClass(topic, topicClassRelationNews.toList))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within TopicService.topicDetail($tid): ${e.getMessage}")
        None
    }
  }

}