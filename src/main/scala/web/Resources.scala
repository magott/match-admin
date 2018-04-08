package web

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.{Html5, Ok, Pass}
import service.{DbRepo, MailgunService, MatchService, MongoRepository}
import conf.Config
import doobie.Transactor
import cats.effect.IO


import scala.util.Properties

class Resources(config: Config, tx: Transactor[IO]) extends Plan{

  private val repo = MongoRepository.singletonWithSessionCaching
  implicit val c = config
  val mailgun = new MailgunService(c)
  val loginHandler = new LoginHandler(repo, mailgun)
  val matchService = new MatchService(repo, DbRepo(tx))
  val webhookHandler = new WebhookHandler(Properties.envOrNone("MAILGUN_API_KEY").get, matchService)
  def intent = {
    case r@GET(_) & XForwardProto("http") => HerokuRedirect(r,r.uri)
    case r@Path(Seg(List("admin", _*))) => new AdminHandler(matchService, mailgun).handleAdmin(r)
    case r@Path(Seg(List("users", _*))) => new UserHandler(repo).handleUser(r)
    case r@Path(Seg(List("matches", _*))) => new MatchHandler(repo).handleMatches(r)
    case r@Path(Seg(List("clubs", _*))) => new ClubHandler(repo, mailgun).handleClubRequest(r)
    case r@Path(Seg(List("webhook", _*))) => webhookHandler.signed(r)
    case r@Path("/login") => loginHandler.handleLogin(r)
    case r@Path("/logout") => loginHandler.handleLogout(r)
    case r@Path("/resetpassword") => loginHandler.handlePasswordReset(r)
    case r@Path("/lostpassword") => loginHandler.handleLostPassword(r)
    case r@Path("/favicon.ico") => new FaviconPlan(c.favicon).handleFavicon(r)
    case r@Path("/") => HerokuRedirect(r, "/matches")
    case r@(Path(p)) => {
      Pass
    }

  }
}
