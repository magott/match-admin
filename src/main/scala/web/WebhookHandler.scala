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
    case r@Path(Seg("webhook"::"mailgun" :: event :: Nil)) & MultiPart(p) =>
      val params = MultiPartParams.Memory(r).params
      val signParams = SignatureParams.unapply(params)
      val mgParams = Mailgunparams.unapply(params)
      handle(r, mgParams, signParams, handleMailgun)

    case r@Path(Seg("webhook"::"mailgun" :: event :: Nil)) & Params(params) =>
      val signParams = SignatureParams.unapply(params)
      val mgParams = Mailgunparams.unapply(params)
      handle(r, mgParams, signParams, handleMailgun)

  }

  def handle(req: HttpRequest[_], mgparams:Option[Mailgunparams], signparams:Option[SignatureParams], reqHandler: (HttpRequest[_], Mailgunparams) => ResponseFunction[HttpServletResponse]) = {
    signparams.filter(_.isValid(apiKey))
      .map(_ => mgparams.map(p => reqHandler(req, p)).getOrElse(Ok))
    .getOrElse(NotAcceptable)
  }

  def handleMailgun(req: HttpRequest[_], mp:Mailgunparams) : ResponseFunction[HttpServletResponse] = {
    req match {
      case Path(Seg("webhook" :: "mailgun" :: "delivered" :: Nil)) =>
        val delivered = MailgunEvent.delivered(mp)
        matchService.saveEvent(delivered)
        Ok
      case Path(Seg("webhook" :: "mailgun" :: "opened" :: Nil))  =>
        val event = MailgunEvent.opened(mp)
        matchService.saveEvent(event)
        Ok
      case Path(Seg("webhook" :: "mailgun" :: "failed" :: Nil))  =>
        val event = MailgunEvent.failed(mp)
        matchService.saveEvent(event)
        Ok
      case Path(Seg("webhook" :: "mailgun" :: "unsubscribed" :: Nil)) =>
        val event = MailgunEvent.unsubscribed(mp)
        matchService.saveEvent(event)
        Ok
      case _ =>
        println("Uhåndtert mailgun event")
        Ok
    }
  }


  object MailgunEvent{
    def delivered(mp:Mailgunparams) =
      MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' levert til ${mp.recipient}", Json.obj().noSpaces, MailDelivered, OkLevel)
    def opened(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' lest av ${mp.recipient}", "{}", MailOpened, OkLevel )
    def failed(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"Mail '${mp.subject}' kunne ikke leveres til ${mp.recipient}${mp.description.map(", fordi: " + _).getOrElse("")}", "{}", MailUnsubscribed, WarnLevel )
    def unsubscribed(mp: Mailgunparams) = MatchEvent(None, User.system, mp.matchId, mp.messageId, mp.timestamp.toLocalDateTime, s"${mp.recipient} har bedt om å ikke få mer mail og vil ikke få det", "{}", MailBounced, ErrorLevel )
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
      messageId <- param("Message-Id").map(_.stripPrefix("<").stripSuffix(""))

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