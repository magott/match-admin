package data

import java.time.LocalDateTime
import io.circe.syntax._
import io.circe.Json

/**
  *
  */
case class MatchEvent(id:Option[Long], endretAv: User,matchId:String, timestamp: LocalDateTime, description:String, details:String) {


}

object MatchEvent{
  def oppsattDommer(m: Match) = {
//    val dommerString = List(m.appointedRef.map(d=> s"HD ${d.name}"),)
    MatchEvent(None, m.idString, LocalDateTime.now(),
      s"""Satt opp HD ${}
          ${m.appointedAssistant1.map(ad1 => s"AD1 ${ad1.name}").getOrElse("")}
        ${m.appointedAssistant2.map(ad1 => s"AD1 ${ad1.name}").getOrElse("")}""",
      m.asJson.noSpaces)
  }

  def kampRegistrert(m: MatchTemplate): MatchEvent ={
//    MatchEvent(None, )
    ???
  }

  def kampPublisert(m: Match): MatchEvent ={
    MatchEvent(None, m.idString, LocalDateTime.now(), "Kamp publisert", "matchjson")
  }
}
