package data

import io.circe.generic.semiauto.deriveEncoder

import java.time.LocalDateTime
import java.util.UUID

import doobie.Meta
import io.circe.Json.JString
import io.circe.syntax._
import io.circe.{Encoder, Json}

/**
  *
  */
case class MatchEvent(id:Option[Long], endretAv: String, matchId:String, uuid:String, timestamp: LocalDateTime, description:String, details:Json, typ:EventType, level: EventLevel, recipient:Option[String])

sealed abstract class EventLevel(private val level:String)
case object SuccessLevel extends EventLevel("success")
case object OkLevel extends EventLevel("ok")
case object WarnLevel extends EventLevel("warn")
case object ErrorLevel extends EventLevel("error")
object EventLevel{
  def fromDb(string:String) = string match {
    case "success" => SuccessLevel
    case "ok" => OkLevel
    case "warn" => WarnLevel
    case "error" => ErrorLevel
  }
  implicit val EventLevelMeta: Meta[EventLevel] = Meta[String].xmap(
    varchar => fromDb(varchar),
    eventlevel => eventlevel.level
  )

  implicit val encodeEventLevel: Encoder[EventLevel] = Encoder.encodeString.contramap(_.level)
}

sealed abstract class EventType(val typ:String, val label:String)
case object MailDelivered extends EventType("delivered", "E-post mottatt")
case object MailOpened extends EventType("opened", "E-post sendt")
case object MailBounced extends EventType("bounced", "E-post feilet")
case object MailUnsubscribed extends EventType("unsubscribed", "Ønsker ikke e-post")
case object MatchEdit extends EventType("edit", "Kamp endret")
object EventType{
  implicit val EventTypeMeta: Meta[EventType] = Meta[String].xmap(
    varchar => varchar match {
      case "delivered" => MailDelivered
      case "opened" => MailBounced
      case "bounced" => MailOpened
      case "unsubscribed" => MailUnsubscribed
      case "edit" => MatchEdit
    },
    eventtype => eventtype.typ
  )
  implicit val encodeEventType: Encoder[EventType] = Encoder.encodeString.contramap(_.label)
}

object MatchEvent{

    implicit val matchEventEncoder : Encoder[MatchEvent] = deriveEncoder[MatchEvent]

  def oppsattDommer(m: Match, user: User) = {
    MatchEvent(None, user.email ,m.idString, UUID.randomUUID().toString, LocalDateTime.now(),
      s"""Nytt dommeroppsett: ${m.refString}""",
      m.asJson, MatchEdit, OkLevel, Some(user.email))
  }

  def kampRegistrert(m: MatchTemplate): MatchEvent ={
//    MatchEvent(None, )
    ???
  }

  def kampPublisert(m: Match, user: User): MatchEvent ={
    MatchEvent(None, user.email, m.idString, UUID.randomUUID().toString, LocalDateTime.now(), "Kamp publisert", Json.obj(), MatchEdit, OkLevel, Some(user.email))
  }
}
