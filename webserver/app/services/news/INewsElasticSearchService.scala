package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsEsRow, NewsFeedResponse, NewsRow }
import utils.EsDriver

import scala.concurrent.Future
import com.sksamuel.elastic4s._
import org.elasticsearch.index.query.{ BoolQueryBuilder, QueryBuilders }
import org.elasticsearch.search.sort.{ SortBuilders, SortOrder }
import play.api.Logger
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[NewsEsService])
trait INewsElasticSearchService {
  def search(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsFeedResponse], Long)]
  def insert(newsRow: NewsRow): Future[Option[Boolean]]
}

class NewsEsService @Inject() extends INewsElasticSearchService with ElasticDsl {

  def search(key: String, pname: Option[String], channel: Option[Long], page: Int, count: Int): Future[(Seq[NewsFeedResponse], Long)] = {
    EsDriver.esClient.execute {
      var boolQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.matchQuery("title", key))
        .should(QueryBuilders.termQuery("tags", key))
      val pnameQuery = pname match {
        case Some(pname) => boolQuery.must(QueryBuilders.termQuery("pname", pname))
        case None        => boolQuery
      }
      val channelQuery = channel match {
        case Some(channel) => pnameQuery.must(QueryBuilders.termQuery("channel", channel))
        case None          => pnameQuery
      }

      search("index_news" / "type_news")
        .query2(channelQuery)
        .sourceInclude("nid", "docid", "title", "ptime", "pname", "purl", "channel", "collect", "concern", "comment", "style", "imgs", "province", "city", "district")
        .start((page - 1) * count).limit(count)
        .sort2(SortBuilders.fieldSort("_score").order(SortOrder.DESC))
        .highlighting(highlight("title").preTag("<font color='#0091fa' >").postTag("</font>"))
    }.map { o =>
      (o.as[NewsFeedResponse].toSeq, o.totalHits)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsEsService.search($key): ${e.getMessage}")
        (Seq[NewsFeedResponse](), 0L)
    }
  }

  def searchHotNid(key: String, page: Int, count: Int): Future[Long] = {
    EsDriver.esClient.execute {
      var boolQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.matchQuery("title", key))
        .should(QueryBuilders.termQuery("tags", key))

      search("index_news" / "type_news")
        .query2(boolQuery)
        .sourceInclude("nid")
        .start((page - 1) * count).limit(count)
        .sort2(SortBuilders.fieldSort("_score").order(SortOrder.DESC))
    }.map { o =>
      (o.hits.head.getSource.get("nid").toString.toLong)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsEsService.searchHotNid($key): ${e.getMessage}")
        0L
    }
  }

  def insert(newsRow: NewsRow): Future[Option[Boolean]] = {
    EsDriver.esClient.execute {
      index("index_news", "type_news").source(Json.toJson(NewsEsRow.from(newsRow)).toString)
    }.map(r => Some(r.isCreated)).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsEsService.insert(${newsRow.base.url}): ${e.getMessage}")
        None
    }
  }
}