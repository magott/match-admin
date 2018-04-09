package web

import java.nio.charset.StandardCharsets
import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletResponse

import data._
import io.circe.syntax._
import io.circe.Json
import service.MatchService
import unfiltered.filter.Plan
import unfiltered.filter.request.{MultiPart, MultiPartParams}
import unfiltered.request._
import unfiltered.response.{Accepted, BadRequest, NotAcceptable, NotFound, Ok, Pass, ResponseFunction}

import scala.util.Try

/**
  *
  */
class WebhookHandler(private val apiKey:String, matchService: MatchService) {

  def signed : Plan.Intent = {
    case r@Path(Seg("webhook"::"mailgun" :: event :: Nil)) =>
      val params = r match {
        case MultiPart(_) => MultiPartParams.Memory(r).params
        case Params(p) => p
      }
      if(SignatureParams.unapply(params).exists(_.isValid(apiKey))) {
        for {
          p <- Mailgunparams.unapply(params)
          creator <- MailgunEvent.get(event)
        } matchService.saveEvent(creator(p))
        Ok
      } else
        NotAcceptable
  }


  object MailgunEvent{
    def get(event:String) : Option[Mailgunparams => MatchEvent] =
      event match {
      case "delivered" => Some(MailgunEvent.delivered)
      case "opened" => Some(MailgunEvent.opened)
      case "failed" => Some(MailgunEvent.failed)
      case "unsubscribed" => Some(MailgunEvent.unsubscribed)
      case _ =>
        println("Uhåndtert mailgun event")
        None
    }
    def delivered(mp:Mailgunparams) =
      MatchEvent(None, User.system.email, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' levert til ${mp.recipient}", Json.obj(), MailDelivered, SuccessLevel, Some(mp.recipient))
    def opened(mp: Mailgunparams) = MatchEvent(None, User.system.email, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' lest av ${mp.recipient}", Json.obj(), MailOpened, SuccessLevel, Some(mp.recipient))
    def failed(mp: Mailgunparams) = MatchEvent(None, User.system.email, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' kunne ikke leveres til ${mp.recipient}${mp.description.map(", fordi: " + _).getOrElse("")}", Json.obj(), MailUnsubscribed, ErrorLevel, Some(mp.recipient) )
    def unsubscribed(mp: Mailgunparams) = MatchEvent(None, User.system.email, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"${mp.recipient} har bedt om å ikke få mer mail og vil ikke få det", Json.obj(), MailBounced, WarnLevel, Some(mp.recipient) )
  }

  def toZonedDateTime(timestamp:String) = {
    Instant.ofEpochSecond(timestamp.toLong).atZone(ZoneId.of("Europe/Oslo"))
  }
}

object Mailgunparams{
  def unapply(p: (String) => Seq[String]): Option[Mailgunparams] = {
    def param(name:String) = p(name).headOption

    for {
      timestamp <- param("timestamp").flatMap(toZonedDateTime)
      recipient <- param("recipient")
      matchId <- param("matchId")
      subject <- param("subject")
      messageId <- param("Message-Id").orElse(param("message-id")).map(_.stripPrefix("<").stripSuffix(""))

    }yield(Mailgunparams(timestamp, recipient, matchId, subject, messageId, param("description")))
  }

  def toZonedDateTime(timestamp:String) = {
    Try(Instant.ofEpochSecond(timestamp.toLong).atZone(ZoneId.of("Europe/Oslo"))).toOption
  }
}

case class Mailgunparams(timestamp:ZonedDateTime, recipient:String, matchId:String, subject:String, messageId:String, description: Option[String])


object SignatureParams{
  def unapply(p: (String) => Seq[String]): Option[SignatureParams] = {
    def param(name:String) = p(name).headOption
    for {
      timestamp <- param("timestamp").flatMap(Mailgunparams.toZonedDateTime)
      signature <- param("signature")
      token <- param("token")
    } yield(SignatureParams(timestamp, signature, token))
  }
}

case class SignatureParams(timestamp: ZonedDateTime, signature:String, token:String){
  def isValid(key:String): Boolean ={
    val validDate = timestamp.toInstant.isBefore(Instant.now().plus(2, ChronoUnit.HOURS))
    val hmac = generateHMAC(key, timestamp.toEpochSecond + token)
    validDate && hmac == signature
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