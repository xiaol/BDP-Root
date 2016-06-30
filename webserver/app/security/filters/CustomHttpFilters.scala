package security.filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.csrf.CSRFFilter
import play.filters.hosts.AllowedHostsFilter

/**
 * Created by zhange on 2016-04-21.
 *
 */

class CustomHttpFilters @Inject() (allowedHostsFilter: AllowedHostsFilter, cors: CORSFilter, csrf: CSRFFilter) extends HttpFilters { //(log: LoggingFilter, csrf: CSRFFilter)

  def filters = Seq(allowedHostsFilter, cors, csrf)

}
