package web

import conf.Config
import data.{User, Referee, Match}
import unfiltered.request._
import service.MongoRepository._
import org.joda.time.{DateMidnight, DateTime}
import unfiltered.response._
import org.bson.types.ObjectId
import unfiltered.response.Html5
import scala.Some
import java.net.URI
import service.MongoRepository

import scala.concurrent.{Await, Future}

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
          case Some(m) => {
            val userId = UserSession.unapply(req).map(_.userId.toString)
            if(userId.exists(m.isAppointed)){
              val appointees = fetchAppointedUsers(m)
              Ok ~> Html5 (Pages(req).viewMatchWithContacts(m, userId.get, appointees))
            }else{
              Ok ~> Html5 (Snippets(req).viewMatch(m, userId))
            }
          }
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

  def fetchAppointedUsers(m: Match): Tuple3[Option[User], Option[User], Option[User]] = {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    val futRef = Future(m.appointedRef.flatMap(ref => repo.userById(ref.id)))
    val futAss1 = Future(m.appointedAssistant1.flatMap(ass1 => repo.userById(ass1.id)))
    val futAss2 = Future(m.appointedAssistant2.flatMap(ass2 => repo.userById(ass2.id)))
    val completed = for {
      ref <- futRef
      ass1 <- futAss1
      ass2 <- futAss2
    } yield (ref, ass1, ass2)
    Await.result(completed, 5.seconds)
  }

  object RefTypeParam extends Params.Extract("reftype", Params.first)

}
