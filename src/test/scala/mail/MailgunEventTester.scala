package mail

import io.circe.Json
import io.circe.Json.JString
import io.circe.parser._

/**
  *
  */
object MailgunEventTester extends App{

  val messageHeaders = """[["X-Mailgun-Sending-Ip", "184.173.153.48"], ["X-Mailgun-Sid", "WyJkZDBmZSIsICJjb2RlQGFuZGVyc2VuLWdvdHQuY29tIiwgIjZjOTIxOCJd"], ["Received", "by luna.mailgun.net with HTTP; Tue, 16 Jan 2018 21:36:37 +0000"], ["Date", "Tue, 16 Jan 2018 21:36:37 +0000"], ["Sender", "ofdl@andersen-gott.com"], ["Message-Id", "<20180116213637.1.B883675AE2BB798A@andersen-gott.com>"], ["X-Mailgun-Variables", "{\"matchId\": \"57ef66d7d4c678bc98f91aba\"}"], ["To", "DEV treningskamp <ofdl@andersen-gott.com>"], ["From", "DEV treningskamp <ofdl@andersen-gott.com>"], ["Subject", "Dommeroppsett klart"], ["Cc", "code@andersen-gott.com"], ["Mime-Version", "1.0"], ["Content-Type", ["multipart/alternative", {"boundary": "e3cc45879c8740fb95edf6957cc946d6"}]]]"""

  private val json = parse(messageHeaders)
  private val array = json.right.get.asArray.get.map(_.asArray.get)
  private val find = array.find(_.head.asString.get == "X-Mailgun-Variables")

  private val hcursor = json.right.get.hcursor
  private val arr = hcursor.downArray

  arr.values.foreach(_.foreach(println))

  private val toMap = array.map(v => (v.head.asString.get -> v.tail.head)).toMap

//  array.foreach(println)
  toMap.foreach(println)

}
