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

    case r@Path("/hello") => Html5(Snippets(r).bootstrap("hello", <h1>{"hello"}</h1>))
    case r@Path("/users/me") =>{
      val u = User(None,"", "er @ kalj.com", "", "", false, DateTime.now, "")
      Html5(Snippets(r).editUserForm(Some(u)))
    }

    //Create new or modify current
//    case _ => NotFound
  }
}
