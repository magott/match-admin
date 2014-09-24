package web

import unfiltered.filter.Plan
import unfiltered.filter.Plan.Intent
import unfiltered.request.Path
import unfiltered.response.Pass

/**
 *
 */
class FaviconPlan(fdlFaviconPath: String) extends Plan{

  override def intent: Intent = {
    case req@Path("/favicon.ico") => {
      if(fdlFaviconPath.equals("/favicon.ico")) Pass
      else HerokuRedirect(req, fdlFaviconPath)
    } 
  }
}
