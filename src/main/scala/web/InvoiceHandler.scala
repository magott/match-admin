package web

import io.circe.Json
import io.circe.syntax._
import org.bson.types.ObjectId
import service.{MatchService, SendRegning}
import unfiltered.request._
import unfiltered.response.{BadRequest, NoContent, NotFound, Ok, ResponseString}
/**
  *
  */
class InvoiceHandler (matchService: MatchService) {

  def handle(req: HttpRequest[_]) = {
    if(matchService.sendRegning.isEmpty) {
      NotFound ~> ResponseString("SendRegning ikke konfigurert")
    }else {
      val sendRegning = matchService.sendRegning.get
      req match {
        case Path(Seg("admin" :: "invoice" :: nr :: Nil)) & GET(_) => {
          sendRegning.finnSendRegningWebUrl(nr.toInt).map(x => Json.obj("status" := x._1, "url" := x._2))
            .map(json => Ok ~> ResponseString(json.noSpaces)).getOrElse(NotFound)
        }
        case r@Path(Seg("admin" :: "invoice" :: "match" :: "new" :: Nil)) => NotFound
        case r@Path(Seg("admin" :: "invoice" :: "match" :: matchId :: Nil)) => r match {
          case GET(_) =>
            matchService.repo.fullMatch(new ObjectId(matchId))
              .flatMap(m => m.invoice.map(a => Json.obj("invoiceNumber" := a)))
              .map(json => Ok ~> ResponseString(json.noSpaces))
              .getOrElse(NoContent)
          case POST(_) & LoggedOnUser(user) =>
            matchService.repo.fullMatch(new ObjectId(matchId))
                .map(matchService.opprettRegning(_, user))
                .map(result =>
                  result.fold(error => BadRequest ~> ResponseString(error),
                              invoice => Ok ~> ResponseString(Json.obj("invoiceNumber" := invoice).noSpaces)))
                  .getOrElse(NotFound)
        }
      }
    }

  }

}
