package web

import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.request.{HttpRequest, Path}
import unfiltered.response.Pass

/**
 *
 */
class FaviconPlan(fdlFaviconPath: String) {

  def handleFavicon(req: HttpRequest[_]) = {
    req match {
      case Path("/favicon.ico") => {
        if (fdlFaviconPath.equals("/favicon.ico")) {
          Pass
        }
        else {
          HerokuRedirect(req, fdlFaviconPath)
        }
      }
      case _ => Pass
    }
  }
}
