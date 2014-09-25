package web

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.{Pass, Ok, Html5}
import service.{MailgunService, MongoRepository}
import conf.Config

class Resources(config: Config) extends Plan{

  private val repo = MongoRepository.singletonWithSessionCaching
  implicit val c = config
  val mailgun = new MailgunService(c)
  val loginHandler = new LoginHandler(repo, mailgun)
  def intent = {
    case r@GET(_) & XForwardProto("http") => HerokuRedirect(r,r.uri)
    case r@Path(Seg(List("admin", _*))) => new AdminHandler(repo, mailgun).handleAdmin(r)
    case r@Path(Seg(List("users", _*))) => new UserHandler(repo).handleUser(r)
    case r@Path(Seg(List("matches", _*))) => new MatchHandler(repo).handleMatches(r)
    case r@Path(Seg(List("clubs", _*))) => new ClubHandler(repo, mailgun).handleClubRequest(r)
    case r@Path("/login") => loginHandler.handleLogin(r)
    case r@Path("/logout") => loginHandler.handleLogout(r)
    case r@Path("/resetpassword") => loginHandler.handlePasswordReset(r)
    case r@Path("/lostpassword") => loginHandler.handleLostPassword(r)
    case r@Path("/favicon.ico") => new FaviconPlan(c.favicon).handleFavicon(r)
    case r@Path("/") => HerokuRedirect(r, "/matches")
    case r@(Path(p)) => {
      println(p)
      Pass
    }

  }
}
