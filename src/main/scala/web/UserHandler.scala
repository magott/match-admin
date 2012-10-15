package web

import unfiltered.request.{GET, Path, HttpRequest}
import unfiltered.response.{ResponseString, NotFound}
import service.MongoRepository
import javax.servlet.http.HttpServletRequest

class UserHandler {

  def handleUser(req: HttpRequest[HttpServletRequest])  = {
    req match {
      case Path("/users/me") => {
        "sdf"
//        req match{
//          case GET =>{
//            //            new MongoRepository().
//            NotFound ~> ResponseString("fuckface!")
//          }
//        }
      }
    }
  }

}
