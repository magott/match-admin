package service

import data.Match
import io.circe.{Decoder, Json, Printer}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import io.circe.syntax._
import io.circe.parser._
import org.bson.types.ObjectId

import scala.io.Source

/**
  *
  */
class SendRegningTest extends FunSuite{

  test("Kan parse en mottaker"){
    val jsonString = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sendregning-recipient.json")).getLines().mkString
    val recipient = io.circe.parser.parse(jsonString).right.flatMap(Recipient.recipientDecoder.decodeJson)
    assert(recipient.isRight)
    println(recipient.right.get)
  }

  test("Kan parse mange mottakere"){
    val jsonString = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sendregning-recipient-all.json")).getLines().mkString("")

    val result = Recipient.fromAll(jsonString)
    assert(result.isRight)
    println(result.right.get)

  }

  test("Kan parse utkast"){
    val jsonString = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sendregning-drafts-all.json")).getLines().mkString("")

    val resultat = io.circe.parser.decode[List[Draft]](jsonString)
    assert(resultat.isRight)
    println(resultat.right.get)
  }

  test("Lager korrekte regningslinjer"){

    val senTrio = Match(Some(new ObjectId(DateTime.now().toDate)), DateTime.now, "hjemme", "borte", "der","men4div", None, DateTime.now.plusDays(6),
      "trio", Some(100), Some(50), Nil, Nil, None, None, None, true, true, None, "payer@example.com", Some("home"), None)

    val tidligTrio = senTrio.copy(kickoff = DateTime.now.plusWeeks(1))

    val senDommer = senTrio.copy(refereeType = "dommer")
    val tidligDommer = tidligTrio.copy(refereeType = "dommer")

    assert(PrisKalkulator.varelinje(senTrio).productCode == "3")
    assert(PrisKalkulator.varelinje(tidligTrio).productCode == "1")
    assert(PrisKalkulator.varelinje(senDommer).productCode == "4")
    assert(PrisKalkulator.varelinje(tidligDommer).productCode == "2")
  }

  test("Kan encode/decode Item"){
    val printer = Printer.spaces2.copy(dropNullValues = true)
    val asJson = Item(1, "1", "desc", 100, 0).asJson
    println(printer.pretty(asJson))
    val item = decode[Item](asJson.noSpaces)
    println(item)
    assert(item.isRight)
    println(item.right.get)
  }

  test("Kan encode/decode Draft"){
    val asJson = Draft(None, None, Recipient.withEmail(None, "Morten", "morten@example.com", Address("1358", "jar")), Item(1,"2", "Desc", 100, 0) :: Nil).asJson
    println(asJson.noSpaces)
    val draft = decode[Draft](asJson.noSpaces)
    assert(draft.isRight)
  }

  test("Kan parse mange Invoices"){
    val jsonString = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sendregning-invoice-all.json")).getLines().mkString("")

    val result = decode[List[Invoice]](jsonString)
    assert(result.isRight)
    println(result.right.get)

  }


}
