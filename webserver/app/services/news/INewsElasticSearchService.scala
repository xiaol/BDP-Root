package services.news

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.{ NewsFeedResponse, NewsRow, NewsEsRow }
import utils.EsDriver

import scala.concurrent.Future
import com.sksamuel.elastic4s._
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.{ SortBuilders, SortOrder }
import play.api.Logger
import play.api.libs.json.Json

import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[NewsEsService])
trait INewsElasticSearchService {
  def search(keywords: String, page: Int, count: Int): Future[Seq[NewsFeedResponse]]
  def insert(newsRow: NewsRow): Future[Option[Boolean]]
}

class NewsEsService @Inject() extends INewsElasticSearchService with ElasticDsl {

  def search(key: String, page: Int, count: Int): Future[Seq[NewsFeedResponse]] = {
    EsDriver.esClient.execute {
      search("index_news" / "type_news")
        .query2(QueryBuilders
          .boolQuery()
          .should(QueryBuilders.matchQuery("title", key))
          .should(QueryBuilders.termQuery("tags", key)))
        .sourceInclude("nid", "docid", "title", "ptime", "pname", "purl", "channel", "collect", "concern", "comment", "style", "imgs", "province", "city", "district")
        .start((page - 1) * count).limit(count)
        .sort2(SortBuilders.fieldSort("ptime").order(SortOrder.DESC), SortBuilders.fieldSort("_score").order(SortOrder.DESC))
        .highlighting(highlight("title").preTag("<font color='#0091fa' >").postTag("</font>"))
    }.map(_.as[NewsFeedResponse].toSeq).recover {
      case NonFatal(e) =>
        Logger.error(s"Within NewsEsService.search($key): ${e.getMessage}")
        Seq[NewsFeedResponse]()
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