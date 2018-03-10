package data

import java.time.LocalDateTime
import java.util.UUID

import io.circe.syntax._
import io.circe.Json

/**
  *
  */
case class MatchEvent(id:Option[Long], endretAv: User, matchId:String, uuid:String, timestamp: LocalDateTime, description:String, details:String, typ:EventType, level: EventLevel) {

}

sealed abstract class EventLevel(level:String)
case object OkLevel extends EventLevel("ok")
case object WarnLevel extends EventLevel("warn")
case object ErrorLevel extends EventLevel("error")

sealed abstract class EventType(typ:String)
case object MailDelivered extends EventType("delivered")
case object MailOpened extends EventType("opened")
case object MailBounced extends EventType("bounced")
case object MailUnsubscribed extends EventType("unsubscribed")
case object MatchEdit extends EventType("edit")

object MatchEvent{
  def oppsattDommer(m: Match, user: User) = {
    MatchEvent(None, user ,m.idString, UUID.randomUUID().toString, LocalDateTime.now(),
      s"""Nytt dommeroppsett: ${m.refString}""",
      m.asJson.noSpaces, MatchEdit, OkLevel)
  }

  def kampRegistrert(m: MatchTemplate): MatchEvent ={
//    MatchEvent(None, )
    ???
  }

  def kampPublisert(m: Match, user: User): MatchEvent ={
    MatchEvent(None, user, m.idString, UUID.randomUUID().toString, LocalDateTime.now(), "Kamp publisert", "{}", MatchEdit, OkLevel)
  }
}
