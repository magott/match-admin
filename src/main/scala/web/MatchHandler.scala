package web

import unfiltered.request.{GET, Path, HttpRequest}
import unfiltered.response.{ResponseString, Ok}

class MatchHandler {

  def handleAdmin(req: HttpRequest[_]) = {
    req match {
      case Path("/matches") => req match{
        case GET(_) => Ok ~> ResponseString("Her kommer liste over kamper")
      }

    }
  }

}
