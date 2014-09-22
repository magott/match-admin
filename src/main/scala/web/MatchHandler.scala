package web

import conf.Config
import unfiltered.request._
import service.MongoRepository._
import org.joda.time.{DateMidnight, DateTime}
import unfiltered.response._
import org.bson.types.ObjectId
import unfiltered.response.Html5
import scala.Some
import java.net.URI
import service.MongoRepository

class MatchHandler(private val repo:MongoRepository) (implicit val config:Config){

  def handleMatches(req: HttpRequest[_]) = {
    req match {
      case Path(Seg("matches" :: Nil)) => req match {
        case GET(_) => {
          val matches = repo.listUpcomingMatches
          Ok ~> Html5(Pages(req).listMatches(matches, "/matches/", UserSession.unapply(req).map(_.userId.toString)))
        }
      }
      case (Path(Seg("matches" :: matchId :: Nil))) => req match {
        case GET(_) => repo.fullMatch(new ObjectId(matchId)) match {
          case Some(m) => Ok ~> Html5 (Snippets(req).viewMatch(m, UserSession.unapply(req).map(_.userId.toString)))
          case None => Html5(Pages(req).notFound(Some("Ingen kamp med id "+matchId)))
        }
        case POST(_) => req match{
          case LoggedOnUser(user) => req match {
            case Params(RefTypeParam("ref")) =>{
              if(repo.refInterestedInMatch(new ObjectId(matchId), user.id.get))
                Ok ~> JsonContent ~> ResponseString("""{"newState": "interested"}""")
              else InternalServerError
            }
            case Params(RefTypeParam("assRef")) =>{
              if(repo.assistantInterestedInMatch(new ObjectId(matchId), user.id.get))
                Ok ~> JsonContent ~> ResponseString("""{"newState": "interested"}""")
              else InternalServerError
            }
            case _ => BadRequest~> JsonContent ~> ResponseString("""{"error":"Invalid or no reftype specified" }""")
          }
          case _ => Forbidden ~> JsonContent~> ResponseString("""{"error": "User not logged in"}""")
        }
        case DELETE(_) => req match{
          case LoggedOnUser(user) => req match {
            case Params(RefTypeParam("ref")) =>{
              if(repo.cancelInterestAsReferee(new ObjectId(matchId), user.id.get))
                Ok ~> JsonContent ~> ResponseString("""{"newState": "not-interested"}""")
              else InternalServerError
            }
            case Params(RefTypeParam("assRef")) =>{
              if(repo.cancelInterestAsAssistant(new ObjectId(matchId), user.id.get))
                Ok ~> JsonContent ~> ResponseString("""{"newState": "not-interested"}""")
              else InternalServerError
            }
            case _ => BadRequest~> JsonContent ~> ResponseString("""{"error":"Invalid or no reftype specified" }""")
          }
        }
        case _ => MethodNotAllowed
      }


    }
  }

  object RefTypeParam extends Params.Extract("reftype", Params.first)

}
