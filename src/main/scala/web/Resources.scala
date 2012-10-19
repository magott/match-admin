package web

import unfiltered.filter.Plan
import unfiltered.response.{Html5, NotFound}
import unfiltered.request.{Seg, Path}
import data._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import java.util.Date
import unfiltered.response.Html5
import scala.Some

class Resources extends Plan{

  def intent = {
    case r@Path(Seg(List("admin", _*))) => new AdminHandler().handleAdmin(r)
    case r@Path(Seg(List("users", _*))) => new UserHandler().handleUser(r)
    case r@Path(Seg(List("matches", _*))) => new MatchHandler().handleMatches(r)
    case r@Path("/login") => new LoginHandler().handleLogin(r)
    case r@Path("/logout") => new LoginHandler().handleLogout(r)
    case r@Path("/lostPassword") => new LoginHandler().handlePasswordReset(r)

  }
}
