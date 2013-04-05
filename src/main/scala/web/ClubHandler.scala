package web

import service.MongoRepository
import unfiltered.request._
import unfiltered.response._
import unfiltered.response.Html5
import data.MatchValidation

class ClubHandler(repo:MongoRepository) {

  def handleClubRequest(req: HttpRequest[_]) = {
    req match{
      case Path(Seg("clubs"::"matches"::"new"::Nil)) => req match {
        case GET(_) => Ok ~> Html5(Pages(req).clubNewMatch)
        case POST(_) => {
          val Params(p) = req
          handleNewMatchFromClub(p)
          Ok ~> Html5(<html>Post is ay okey!</html>)
       }
      }
      case _ => NotFound ~> Html5(Pages(req).notFound())
    }
  }

  def handleNewMatchFromClub(p: Map[String, Seq[String]]) = {
    p.withDefaultValue(List(""))
//    MatchValidation.unpublished(p("homeTeam").head, p("awayTeam").head, )
    Nil
  }

}
