package web

import java.nio.charset.StandardCharsets
import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

import data.MatchEvent
import io.circe.syntax._
import io.circe.Json
import unfiltered.request.{HttpRequest, Params, Path, Seg}
import unfiltered.response.{NotAcceptable, NotFound, Ok, Pass}

/**
  *
  */
class WebhookHandler(private val apiKey:String) {

  def validate(req: HttpRequest[_]) = {
    val foo:Either[String, String] = Left("foo")
    val p = Params.unapply(req).get
    println(p)

    val timestamp = p("timestamp").head
    val zonedDateTime = Instant.ofEpochSecond(timestamp.toLong).atZone(ZoneId.of("Europe/Oslo"))
    val valid = isValid(p("signature").head, timestamp,p("token").head)
    if(valid){
      val matchId = p("X-Mailgun-Variables").asJson.hcursor.downField("matchId").as[String].right.get
//      MailgunEvent(zonedDateTime, matchId, p())
      Right("right")
    }else {
      Left("left")
    }

  }

  def handle(req: HttpRequest[_]) = {
    req match {
      case Path(Seg("webhook" :: "mailgun" :: "sent" :: Nil)) => {
        val either = validate(req)
        if(either.isLeft) NotAcceptable
        else {
          Ok
        }
      }
      case _ => NotFound
    }
  }

  case class MailgunEvent(timestamp:ZonedDateTime, matchId:String, subject:String, to:String, json:Json) {
    def toMatchEvent = MatchEvent(None, matchId, timestamp.toLocalDateTime, s"Mail $subject mottatt av $to", "")
  }

  def isValid(signature:String, timestamp:String, token:String) = {
    val zonedDateTime = Instant.ofEpochSecond(timestamp.toLong)
    val validDate = zonedDateTime.isBefore(Instant.now().plus(2, ChronoUnit.HOURS))
    val hmac = generateHMAC(apiKey, timestamp + token)
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
