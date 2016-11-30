package services.userprofiles

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.news.NewsRow
import commons.models.news.NewsFeedResponse
import commons.models.userprofiles._
import commons.utils.JodaOderingImplicits
import dao.news.NewsDAO
import dao.userprofiles._
import dao.users.UserDAO
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhange on 2016-05-12.
 *
 */

@ImplementedBy(classOf[ProfileService])
trait IProfileService {
  def addComment(commentRow: CommentRow): Future[Option[Long]]
  def remComment(cid: Long, docid: String): Future[Option[Long]]
  def listCommentsWithNewsInfo(uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]]

  def addCommend(cid: Long, uid: Long): Future[Option[Int]]
  def remCommend(cid: Long, uid: Long): Future[Option[Int]]

  def addCollect(nid: Long, uid: Long): Future[Option[Int]]
  def remCollect(nid: Long, uid: Long): Future[Option[Int]]
  def listCollects(uid: Long, page: Long, count: Long): Future[Seq[NewsFeedResponse]]

  def addConcern(nid: Long, uid: Long): Future[Option[Int]]
  def remConcern(nid: Long, uid: Long): Future[Option[Int]]
  def listConcerns(uid: Long, page: Long, count: Long): Future[Seq[NewsFeedResponse]]

  def updateUserChannels(uid: Long, channels: UserChannels): Future[Option[UserChannels]]

  def addSearch(searchnews: Searchnewslist): Future[Option[Int]]
  def addRelay(relaynews: Relaylist): Future[Option[Int]]
  def addHate(hatenews: Hatenewslist): Future[Option[Int]]
}

class ProfileService @Inject() (val commentDAO: CommentDAO, val newsDAO: NewsDAO, val commendDAO: CommendDAO,
                                val collectDAO: CollectDAO, val concernDAO: ConcernDAO, val userDAO: UserDAO,
                                val searchNewsDAO: SearchNewsDAO, val relayNewsDAO: RelayNewsDAO, val hateNewsDAO: HateNewsDAO)
    extends IProfileService {

  import JodaOderingImplicits.LocalDateTimeReverseOrdering

  def addComment(commentRow: CommentRow): Future[Option[Long]] = {
    commentDAO.insert(commentRow).flatMap { commentId =>
      newsDAO.updateComment(commentRow.docid, 1).map {
        case Some(_) => Some(commentId)
        case _       => None
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addComment(${commentRow.toString}): ${e.getMessage}")
        None
    }
  }

  def remComment(cid: Long, docid: String): Future[Option[Long]] = {
    commentDAO.delete(cid).flatMap {
      case Some(commentId) => newsDAO.updateComment(docid, -1).map {
        case Some(_) => Some(commentId)
        case _       => None
      }
      case None => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.remComment($cid, $docid): ${e.getMessage}")
        None
    }
  }

  def listComments(uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]] = {
    commentDAO.listByUid(uid, (page - 1) * count, count).map { rows =>
      rows.sortBy(_.ctime).map(CommentResponse.from(_, 1))
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.listComments($uid, $page, $count): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def listCommentsWithNewsInfo(uid: Long, page: Long, count: Long): Future[Seq[CommentResponse]] = {
    commentDAO.listByUidWithNewsInfo(uid, (page - 1) * count, count).map { pairs =>
      pairs.sortBy(_._1.ctime).map { pair => CommentResponse.from(pair._1, 1, Some(pair._2), Some(pair._3)) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.listCommentsWithNewsInfo($uid, $page, $count): ${e.getMessage}")
        Seq[CommentResponse]()
    }
  }

  def addCommend(cid: Long, uid: Long): Future[Option[Int]] = {
    val commendRow = CommendRow(None, LocalDateTime.now().withMillisOfSecond(0), uid, cid)
    commendDAO.insert(commendRow).flatMap { _ =>
      commentDAO.updateCommend(cid, uid, 1).map {
        case commend @ Some(_) => commend
        case None              => None
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addCommend($cid, $uid): ${e.getMessage}")
        None
    }
  }

  def remCommend(cid: Long, uid: Long): Future[Option[Int]] = {
    commendDAO.delete(cid, uid).flatMap {
      case Some(_) => commentDAO.updateCommend(cid, uid, -1).map {
        case commend @ Some(_) => commend
        case None              => None
      }
      case None => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.remCommend($cid, $uid): ${e.getMessage}")
        None
    }
  }

  def addCollect(nid: Long, uid: Long): Future[Option[Int]] = {
    val collectRow = CollectRow(None, LocalDateTime.now().withMillisOfSecond(0), uid, nid)
    collectDAO.insert(collectRow).flatMap { _ =>
      newsDAO.updateCollect(nid, 1).map {
        case collect @ Some(_) => collect
        case None              => None
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addCollect($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def remCollect(nid: Long, uid: Long): Future[Option[Int]] = {
    collectDAO.delete(nid, uid).flatMap {
      case Some(_) => newsDAO.updateCollect(nid, -1).map {
        case collect @ Some(_) => collect
        case None              => None
      }
      case None => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.remCollect($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def listCollects(uid: Long, page: Long, count: Long): Future[Seq[NewsFeedResponse]] = {
    collectDAO.listNewsByCollects(uid, (page - 1) * count, count).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.listCollects($uid, $page, $count): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def addConcern(nid: Long, uid: Long): Future[Option[Int]] = {
    val concernRow = ConcernRow(None, LocalDateTime.now().withMillisOfSecond(0), uid, nid)
    concernDAO.insert(concernRow).flatMap { _ =>
      newsDAO.updateConcern(nid, 1).map {
        case collect @ Some(_) => collect
        case None              => None
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addConcern($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def remConcern(nid: Long, uid: Long): Future[Option[Int]] = {
    concernDAO.delete(nid, uid).flatMap {
      case Some(_) => newsDAO.updateConcern(nid, -1).map {
        case collect @ Some(_) => collect
        case None              => None
      }
      case _ => Future.successful(None)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.remConcern($nid, $uid): ${e.getMessage}")
        None
    }
  }

  def listConcerns(uid: Long, page: Long, count: Long): Future[Seq[NewsFeedResponse]] = {
    concernDAO.listNewsByConcerns(uid, (page - 1) * count, count).map {
      case newsRows: Seq[NewsRow] => newsRows.map { r => NewsFeedResponse.from(r) }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.listConcerns($uid, $page, $count): ${e.getMessage}")
        Seq[NewsFeedResponse]()
    }
  }

  def updateUserChannels(uid: Long, channels: UserChannels): Future[Option[UserChannels]] = {
    userDAO.updateChannel(uid, channels.channels).map {
      case Some(chs) => Some(UserChannels(chs))
      case None      => None
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.updateUserChannels($uid, ${channels.toString}): ${e.getMessage}")
        None
    }
  }

  def addSearch(searchnews: Searchnewslist): Future[Option[Int]] = {
    searchNewsDAO.insert(searchnews).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addSearch(${searchnews.toString}): ${e.getMessage}")
        None
    }
  }

  def addRelay(relaynews: Relaylist): Future[Option[Int]] = {
    relayNewsDAO.insert(relaynews.copy(ctime = Some(LocalDateTime.now()))).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addRelay(${relaynews.toString}): ${e.getMessage}")
        None
    }
  }

  def addHate(hatenews: Hatenewslist): Future[Option[Int]] = {
    hateNewsDAO.insert(hatenews.copy(ctime = Some(LocalDateTime.now()))).map { id => Some(id) }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within ProfileService.addHate(${hatenews.toString}): ${e.getMessage}")
        None
    }
  }
}