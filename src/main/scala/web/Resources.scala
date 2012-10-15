package web

import unfiltered.filter.Plan
import unfiltered.response.{Html5, NotFound}
import unfiltered.request.Path
import data.{Men3Div, User}
import org.joda.time.DateTime

class Resources extends Plan{
  def intent = {


    case r@Path("/hello") => Html5(Snippets(r).bootstrap("hello", <h1>{"hello"}</h1>))
    case r@Path("/users/me") =>{
      val u = User(None,"", "er @ kalj.com", "", "", false, DateTime.now, "")
      Html5(Snippets(r).editUserForm(Some(u)))
    }
    case r@Path("/admin/matches/match") => Html5(Snippets(r).editMatch(None))

    //Create new or modify current
//    case _ => NotFound
  }
}
