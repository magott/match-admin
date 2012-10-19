package web

import unfiltered.request.{GET, Path, HttpRequest}
import service.MongoRepository
import org.joda.time.DateTime
import unfiltered.response.{Html5, ResponseString, Ok}

class MatchHandler {

  def handleMatches(req: HttpRequest[_]) = {
    req match {
      case Path("/matches") => req match{
        case GET(_) => {
          val matches = MongoRepository.listMatchesNewerThan(DateTime.now.withDayOfYear(1))
          Ok ~> Html5(Snippets(req).bootstrap("Kamper", <div>Her kommer det kamper</div>))
        }
      }
    }
  }

}
