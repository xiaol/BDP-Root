package commons.models.detail

import commons.models.joke.JokeDetailRow
import commons.models.news.NewsDetailRow
import commons.models.video.VideoDetailRow

/**
 * Created by zhangshl on 17/4/19.
 */
case class DetailRow(news: Option[NewsDetailRow] = None, video: Option[VideoDetailRow] = None, joke: Option[JokeDetailRow] = None)
