package security.filters

import javax.inject.Inject
import akka.stream.Materializer
import controllers.Default
import play.api.mvc.{ Result, RequestHeader, Filter }

/**
 * Created by zhange on 2016-05-31.
 *
 */

class CORSFilter @Inject() (implicit val mat: Materializer) extends Filter {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  lazy val allowedDomain = Some("*")

  def isPreFlight(r: RequestHeader) = (
    r.method.toLowerCase.equals("options")
    &&
    r.headers.get("Access-Control-Request-Method").nonEmpty
  )

  def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if (isPreFlight(request)) {
      Future.successful(Default.Ok.withHeaders(
        "Access-Control-Allow-Origin" -> allowedDomain.orElse(request.headers.get("Origin")).getOrElse(""),
        "Access-Control-Allow-Methods" -> request.headers.get("Access-Control-Request-Method").getOrElse("POST, GET, OPTIONS, PUT, DELETE"), //Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent, Authorization, If-Match
        "Access-Control-Allow-Headers" -> request.headers.get("Access-Control-Request-Headers").getOrElse("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Authorization"),
        "Access-Control-Expose-Headers" -> request.headers.get("Access-Control-Expose-Headers").getOrElse("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Authorization"),
        "Access-Control-Allow-Credentials" -> "true"
      ))
    } else {
      f(request).map {
        _.withHeaders(
          "Access-Control-Allow-Origin" -> allowedDomain.orElse(request.headers.get("Origin")).getOrElse(""),
          "Access-Control-Allow-Methods" -> request.headers.get("Access-Control-Request-Method").getOrElse("POST, GET, OPTIONS, PUT, DELETE"),
          "Access-Control-Allow-Headers" -> request.headers.get("Access-Control-Request-Headers").getOrElse("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Authorization"),
          "Access-Control-Expose-Headers" -> request.headers.get("Access-Control-Expose-Headers").getOrElse("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Authorization"),
          "Access-Control-Allow-Credentials" -> "true"
        )
      }
    }
  }
}
