package service

import java.nio.charset.StandardCharsets
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import io.circe.{Decoder, Encoder, Error, Json, Printer}
import io.circe.syntax._
import scalaj.http.{Http, HttpResponse}
import cats.syntax.either._
import com.google.common.cache.CacheBuilder
import data.{ContactInfo, Match, RefereeType}
import org.joda.time.{DateTime, Days, Hours}
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
import scala.util.{Properties, Try}

//import cats.implicits._
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.either._

/**
  *
  */

object SendRegning {

  val log = LoggerFactory.getLogger(SendRegning.getClass)

  implicit class ScalaJPimp[R](response: HttpResponse[R]){
    def either : Either[String, R] = if (response.is2xx) {
      Right(response.body)
    } else {
      Left(s"${response.code} : ${response.body}")
    }

    def option : Option[R] = if(response.is2xx) Some(response.body) else None

    def eitherResp : Either[String, HttpResponse[R]] =
      if(response.is2xx) Right(response)
      else if (response.code == 401) Left("Fikk ikke tilgang til SendRegning. Om passordet er endret, gi beskjed til Morten")
      else Left(s"${response.code} : ${response.body}")
  }

  def main(args: Array[String]): Unit = {

    val f = Try(Source.fromInputStream(getClass.getResourceAsStream("/Postnummerregister-ansi.txt"), "UTF-8").getLines()
      .map{ s =>
        val splitted = s.split("\t")
        splitted(0) -> splitted(1)
      }.toMap).toOption

    val sendRegning = SendRegning.create.get
//    val resp = sendRegning.hentAlleMottakereResp
//    resp.right.get.foreach(println)
//    val utkast = sendRegning.hentAlleRegningsutkast
//    utkast.foreach(println)
//    val regning = utkast.right.get.head
    val senTrio = Match(None, DateTime.now, "Ørn-Horten", "Stålkameratene", "der","men4div", None, DateTime.now.plusDays(6),
      "trio", Some(100), Some(50), Nil, Nil, None, None, None, true, true, Some(ContactInfo("Payer","","1358","", "foo@bar.com")), "payer@example.com", Some("home"), None)

//    val int = sendRegning.opprettRegningPaKamp(senTrio)
//    println(int)
    sendRegning.finnRegningPaged(334).foreach(println)

  }

  def create : Option[SendRegning] = {
    for {
      username <- Properties.envOrNone("SENDREGNING_USERNAME")
      password <- Properties.envOrNone("SENDREGNING_PASSWORD")
      postnrMap <- Try(Source.fromInputStream(getClass.getResourceAsStream("/Postnummerregister-ansi.txt"), "UTF-8").getLines()
        .map{ s =>
          val splitted = s.split("\t")
          splitted(0) -> splitted(1)
        }.toMap).toOption
    } yield {
      log.info("Applikasjon konfigurert med SendRegning")
      SendRegning(username, password, Properties.envOrNone("SENDREGNING_ORIGINATOR"), postnrMap)
    }
  }
}

case class SendRegning(username:String, password:String, originator:Option[String], postnrMap:Map[String, String]) {

  import SendRegning._
  val baseUrl = "https://www.sendregning.no"
  val recipientCache = CacheBuilder.newBuilder.maximumSize(1).expireAfterWrite(5, TimeUnit.MINUTES).build()
  val printer = Printer.noSpaces.copy(dropNullValues = true)

  def opprettRegningPaKamp(m:Match) : Either[String, Int] = {
    val mottaker = m.regningsMottaker
    val epost = mottaker.map(_.email).get
    val recipient:Either[String, List[Recipient]] = hentAlleMottakereResp.map(_.filter(_.email == epost).headOption.toList)

    type L[A] = Either[String, A]

    def hent(rs:List[Recipient]):Either[String, List[Draft]] =
      rs.flatTraverse[L, Draft](r => hentRegningsutkastFor(r)).map(_.headOption.toList)

    val drafts: Either[String, List[Draft]] = recipient.flatMap(hent)

    //Hvis mottaker og draft, legg til
    if(drafts.exists(_.nonEmpty)) {
      leggTiRegning(drafts.right.get.head, m)
    } else if(recipient.exists(_.nonEmpty)) {
      //Hvis mottaker og ikke draft lag nytt draft
      opprettRegningPaa(recipient.right.get.head, m)
    } else {
      //Hvis ikke mottaker, lag ny regning med ny mottaker
      val maybeInt:Option[Either[String, Int]] = mottaker.map(x => Recipient.newFromContact(x, postnrMap))
        .map(nyMottaker => opprettRegningPaa(nyMottaker, m))

      maybeInt.getOrElse(Left("Trenger navn, epost og postnummer på mottaker av regning"))
    }

  }

  def hentAlleMottakereResp : Either[String, List[Recipient]] = {
    val response = sendRegningUrl("/recipients/all?limit=10000").asString

    if (response.is2xx) {
      Recipient.fromAll(response.body).leftMap(_.getMessage)
    } else {
      Left(s"${response.code} : ${response.body}")
    }
  }

  def hentRegningsutkastFor(recipient: Recipient) : Either[String, List[Draft]] ={
    sendRegningUrl(s"/recipients/${recipient.number.get}/draftDocuments").asString.either.right.flatMap(s => io.circe.parser.decode[List[Draft]](s).leftMap(_.getMessage))
  }

  def hentAlleRegningsutkast : Either[String, List[Draft]] ={
    sendRegningUrl("/invoices/drafts").asString.either.right.flatMap(s => io.circe.parser.decode[List[Draft]](s).leftMap(_.getMessage))
  }

  def opprettRegningPaa(recipient: Recipient, m:Match) : Either[String, Int] = {
    val regning = Draft(None, None, recipient, PrisKalkulator.varelinje(m) :: Nil)

    val resp : Either[String, HttpResponse[String]] = sendRegningUrl("/invoices/drafts").postData(printer.pretty(regning.asJson)).asString.eitherResp
    println(s"Regningsresponse $resp")
    //Teigen
    val createdId = for {
      r <- resp
      location <- r.location.toRight("Regningopprettelse feilet")
      draftId = location.split('/').last.toInt
      draft <- hentUtkast(draftId).toRight("mangler draft")
    } yield {
      val newDraft = draft.copy(orderNo = Some(draftId.toString))
      oppdaterRegning(newDraft)
      draftId
    }

    createdId
  }

  def hentUtkast(draftNo:Int) : Option[Draft] = {
    sendRegningUrl(s"/invoices/drafts/$draftNo").asString.option.flatMap(s => io.circe.parser.decode[Draft](s).toOption)
  }

  def leggTiRegning(draft: Draft, m:Match) = {
    val varelinje = PrisKalkulator.varelinje(m)
    val oppdatert = draft.copy(items = varelinje :: draft.items, orderNo = draft.number.map(_.toString))
    oppdaterRegning(oppdatert).eitherResp.right.map(_ => draft.number.get)
  }

  def oppdaterRegning(draft: Draft) = {
    val string = sendRegningUrl(s"/invoices/drafts/${draft.number.get}").put(printer.pretty(draft.asJson)).asString
    string
  }


  def sendRegningUrl(segments:String) = Http(s"$baseUrl$segments")
    .header("Accept","application/json")
    .header("Content-Type","application/json")
    .header("Originator-Id", originator.getOrElse(""))
    .header("Authorization", authHeader)
    .timeout(10000, 20000)

  def authHeader = s"Basic ${Base64.getEncoder.encodeToString(s"$username:$password".getBytes(StandardCharsets.UTF_8))}"

  def finnSendRegningWebUrl(draftNumber: Int): Option[Tuple2[String, String]] ={
    val draft = sendRegningUrl(s"/invoices/drafts/$draftNumber").asString.option
      .map(_ => s"Opprettet #$draftNumber" -> s"https://www.sendregning.no/side/#/regning/kladd/$draftNumber")

    draft.orElse(finnRegningPaged(draftNumber).map(invoiceNumber => s"Sendt #$invoiceNumber" -> s"https://www.sendregning.no/side/#/regning/$invoiceNumber"))
  }

  def finnRegning(draftNr: Int) : Option[Int] = {
    val regningerJsonString = sendRegningUrl(s"/invoices/").asString.body

    //Kan sikkert optimaliseres ved å kun hente regninger for en mottaker
    val regninger = io.circe.parser.decode[List[Invoice]](regningerJsonString)

    regninger.toOption.flatMap(_.find(_.orderNo.exists(_ == draftNr.toString))).map(_.number)
  }

  def finnRegningPaged(draftNr: Int) : Option[Int] = {
   Stream.from(1)
     .map(page => sendRegningUrl(s"/invoices/?page=$page&perPage=30").asString.body.trim)
     .takeWhile(_.nonEmpty)
     .flatMap{
       pageString =>
       val parsed = io.circe.parser.decode[List[Invoice]](pageString)
       parsed.right.get
     }
     .dropWhile(_.orderNo.forall(orderNumber => notNumber(orderNumber)  || orderNumber.toInt != draftNr))
     .headOption
     .map(_.number)
  }

  def notNumber(i:String) : Boolean = !i.matches("""\d+""")

}

case class Address(zip:String, city:String)
object Address{
  implicit val addressDecoder : Decoder[Address] = Decoder.forProduct2("zip", "city")(apply)
  implicit val addressEncoder : Encoder[Address] = Encoder.forProduct2("zip", "city")(a => (a.zip, a.city))
}

case class Recipient(number:Option[Int], name:String, emailOpt:Option[String], address: Address){
  val email:String = emailOpt.getOrElse("")
}
object Recipient{
  def withEmail(number:Option[Int], name:String, email:String, address: Address) : Recipient = Recipient(number, name, Some(name), address)
  implicit val recipientDecoder:Decoder[Recipient] = Decoder.instance{
    c =>
      for{
        number <- c.downField("number").as[Int]
        name <- c.downField("name").as[String]
        contact <- c.downField("contact").as[Json]
        address <- contact.hcursor.downField("address").as[Address]
        email <- contact.hcursor.downField("email").as[Option[String]]
      } yield {
        Recipient(Some(number), name, email, address)
      }
  }

  implicit val recipientEncoder: Encoder[Recipient] = Encoder.forProduct4("number", "email", "name", "address")(r => (r.number, r.email, r.name, r.address))
  def fromAll(json:String) : Either[Error, List[Recipient]] = {
    val either: Either[Error, List[Either[Error,Recipient]]] = io.circe.parser.parse(json).right.flatMap(_.as(Decoder.instance(_.downField("recipient").as[List[Json]])
      .map(_.map(_.as[Recipient]))))

    either.right.map(_.filter(_.isRight).map(_.right.get))
  }

  def newFromContact(contactInfo: ContactInfo, postnrMap:Map[String, String]) = {
    Recipient(None, contactInfo.name, Some(contactInfo.email), Address(contactInfo.zip, postnrMap.getOrElse(contactInfo.zip, "Ukjent")))
  }

}
case class Item(qty:Int, productCode:String, description:String, unitPrice:Double, taxRate:Int)
object Item{
  implicit val itemDecoder: Decoder[Item] = Decoder.forProduct5("quantity", "productCode", "description", "unitPrice", "taxRate")(apply)
  implicit val itemEncoder: Encoder[Item] = Encoder.forProduct5("quantity", "productCode", "description", "unitPrice", "taxRate")(i => (i.qty, i.productCode, i.description, i.unitPrice, i.taxRate))
}
case class Draft(number:Option[Int], orderNo: Option[String],recipient: Recipient, items:List[Item], invoiceText:Option[String] = Some("Dommer til treningskamp"))
object Draft{
  implicit val recipientInDraftDecoder : Decoder[Recipient] = Decoder.forProduct4("number", "name", "email", "address")(Recipient.apply)
  implicit val draftDecoder : Decoder[Draft] = Decoder.forProduct5("number", "orderNumber", "recipient", "items", "invoiceText")(Draft.apply)
  implicit val draftEncoder : Encoder[Draft] = Encoder.forProduct5("number", "orderNumber", "recipient", "items", "invoiceText")(d => (d.number, d.orderNo, d.recipient, d.items, d.invoiceText))
}

object Invoice{
  import Draft.recipientInDraftDecoder
  implicit val invoiceDecoder : Decoder[Invoice] = Decoder.forProduct3("number", "orderNumber", "recipient")(Invoice.apply)
}
case class Invoice(number:Int, orderNo: Option[String], recipient: Recipient){}

object PrisKalkulator{
  def varelinje(m:Match) : Item = {
    import data.ObjectIdPimp
    val timerIForveien = Hours.hoursBetween(m.id.get.dateTime, m.kickoff).getHours
    if(m.season >= 2019){
      from2019(m, timerIForveien)
    }else{
      pre2019(m, timerIForveien)
    }
  }

  def pre2019(m: Match, timerIForveien:Int) : Item  = {
    Prisgruppe.from(m.refereeType, timerIForveien) match {
      case HastTrio | SenTrio => Item(1, "3", s"Adm. gebyr trio til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - sen bestilling", 250, 0)
      case RegulerTrio => Item(1, "1", s"Adm. gebyr trio til ${m.teams} ${m.kickoff.toString("dd.MM.yy")}", 150, 0)
      case HastKunDommer | SenKunDommer => Item(1, "4", s"Adm. gebyr enkeltdommer til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - sen bestilling", 200, 0)
      case RegulerKunDommer => Item(1, "2", s"Adm. gebyr enkeltdommer til ${m.teams} ${m.kickoff.toString("dd.MM.yy")}", 100, 0)
    }
  }

  private def from2019(m: Match, timerIForveien: Int) : Item = {
    Prisgruppe.from(m.refereeType, timerIForveien) match {
      case HastTrio => Item(1, "8", s"Adm. gebyr trio til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - 48t bestilling", 350, 0)
      case SenTrio => Item(1, "3", s"Adm. gebyr trio til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - sen bestilling", 250, 0)
      case RegulerTrio => Item(1, "1", s"Adm. gebyr trio til ${m.teams} ${m.kickoff.toString("dd.MM.yy")}", 150, 0)
      case HastKunDommer => Item(1, "9", s"Adm. gebyr enkeltdommer til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - 48t bestilling", 300, 0)
      case SenKunDommer => Item(1, "4", s"Adm. gebyr enkeltdommer til ${m.teams} ${m.kickoff.toString("dd.MM.yy")} - sen bestilling", 200, 0)
      case RegulerKunDommer => Item(1, "2", s"Adm. gebyr enkeltdommer til ${m.teams} ${m.kickoff.toString("dd.MM.yy")}", 100, 0)
    }
  }

  sealed class Prisgruppe

  object Prisgruppe{
    def from(refereeType: String, timerIForveien: Int): Prisgruppe = {
      val dagerIForveien = Hours.hours(timerIForveien).toStandardDays.getDays
      if (refereeType == RefereeType.Dommer.key) {
        if (timerIForveien <= 48) HastKunDommer
        else if (dagerIForveien <= 6) SenKunDommer
        else RegulerKunDommer
      }
      else if (refereeType == RefereeType.Trio.key) {
        if (timerIForveien <= 48) HastTrio
        else if (dagerIForveien <= 6) SenTrio
        else RegulerTrio
      }
      else RegulerKunDommer
    }
  }
    case object HastKunDommer extends Prisgruppe
    case object SenKunDommer extends Prisgruppe
    case object RegulerKunDommer extends Prisgruppe

    case object HastTrio extends Prisgruppe
    case object SenTrio extends Prisgruppe
    case object RegulerTrio extends Prisgruppe



}




