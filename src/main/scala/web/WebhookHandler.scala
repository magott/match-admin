package web

import java.nio.charset.StandardCharsets
import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

import data._
import io.circe.syntax._
import io.circe.Json
import service.MatchService
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response.{Accepted, BadRequest, NotAcceptable, NotFound, Ok, Pass}

import scala.util.Try

/**
  *
  */
class WebhookHandler(private val apiKey:String, matchService: MatchService) {

  def signed(mailgunIntent: Plan.Intent) : Plan.Intent = {
    case r@Path(Seg("webhook"::"mailgun" :: event :: Nil)) & Params(p) =>
      println(s"Event: $event : $p")
      if(isValid(p("signature").head, p("timestamp").head, p("token").head))
        mailgunIntent(r)
      else
        NotAcceptable

  }

  def handle: Plan.Intent = {
      case r@Path(Seg("webhook" :: "mailgun" :: "delivered" :: Nil)) & Params(Mailgunparams(mp)) => {
        val delivered = MailgunEvent.delivered(mp)
        matchService.saveEvent(delivered)
        Ok
      }
      case r@Path(Seg("webhook" :: "mailgun" :: "opened" :: Nil)) & Params(Mailgunparams(mp)) =>
        val event = MailgunEvent.opened(mp)
        matchService.saveEvent(event)
        Ok
      case r@Path(Seg("webhook" :: "mailgun" :: "failed" :: Nil)) & Params(Mailgunparams(mp)) =>
        val event = MailgunEvent.failed(mp)
        matchService.saveEvent(event)
        Ok
      case r@Path(Seg("webhook" :: "mailgun" :: "unsubscribed" :: Nil)) & Params(Mailgunparams(mp)) =>
        val event = MailgunEvent.unsubscribed(mp)
        matchService.saveEvent(event)
        Ok
      case _ =>
        println("Uhåndtert mailgun event")
        Ok
  }

  object MailgunEvent{
    def delivered(mp:Mailgunparams) =
      MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail ${mp.subject} levert til ${mp.recipient}", Json.obj().noSpaces, MailDelivered, OkLevel)
    def opened(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail lest av ${mp.recipient}", "{}", MailOpened, OkLevel )
    def failed(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail kunne ikke leveres til ${mp.recipient}${mp.description.map(", fordi: " + _).getOrElse("")}", "{}", MailUnsubscribed, WarnLevel )
    def unsubscribed(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"${mp.recipient} har bedt om å ikke få mer mail. Dette er håndtert", "{}", MailBounced, ErrorLevel )
  }

  def toZonedDateTime(timestamp:String) = {
    Instant.ofEpochSecond(timestamp.toLong).atZone(ZoneId.of("Europe/Oslo"))
  }


  def isValid(signature:String, timestamp:String, token:String) = {
    val zonedDateTime = Instant.ofEpochSecond(timestamp.toLong)
    val validDate = zonedDateTime.isBefore(Instant.now().plus(2, ChronoUnit.HOURS))
    val hmac = generateHMAC(apiKey, timestamp + token)
    val b = validDate && hmac == signature
    if(!b){
      println(s"Ugyldig request $validDate $apiKey")
    }
    b
  }

  def generateHMAC(sharedSecret: String, preHashString: String): String = {
    import scala.collection.JavaConverters._
    val secret = new SecretKeySpec(sharedSecret.getBytes, "HMacSha256")   //Crypto Funs : 'SHA256' , 'HmacSHA1'
    val mac = javax.crypto.Mac.getInstance("HMacSha256")
    mac.init(secret)
    val hashString: Array[Byte] = mac.doFinal(preHashString.getBytes(StandardCharsets.UTF_8))
    hashString.map("%02X" format _).mkString.toLowerCase
  }

}


object Mailgunparams{
  def unapply(p:Map[String, Seq[String]]): Option[Mailgunparams] = {
    def param(name:String) = p(name).headOption

    for {
      timestamp <- param("timestamp").flatMap(toZonedDateTime)
      recipient <- param("recipient")
      matchId <- param("matchId")
      subject <- param("subject")
      messageId <- param("Message-Id").map(_.stripPrefix("<").stripSuffix(""))

    }yield(Mailgunparams(timestamp, recipient, matchId, subject, messageId, param("description")))


  }

  def toZonedDateTime(timestamp:String) = {
    Try(Instant.ofEpochSecond(timestamp.toLong).atZone(ZoneId.of("Europe/Oslo"))).toOption
  }
}

case class Mailgunparams(timestamp:ZonedDateTime, recipient:String, matchId:String, subject:String, messageId:String, description: Option[String])

