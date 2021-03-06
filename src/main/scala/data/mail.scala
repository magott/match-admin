package data

import conf.Config
import io.circe.Json
import io.circe.Json.JString

import xml.NodeSeq

case class MailMessage(from:String, to:Seq[String], cc:Seq[String], bcc:Seq[String], subject:String, body:String, bodyHtml:Option[NodeSeq], matchId: Option[String]) {

  def asMailgunParams : Seq[(String,String)] = Seq("from" -> from, "subject" -> subject, "text"->body) ++ to.map("to"-> _) ++ cc.map("cc" -> _) ++ bcc.map("bcc" -> _) ++
    bodyHtml.map("html" -> _.toString) ++
    matchId.map(id => Seq("v:matchId" -> id)).getOrElse(Seq.empty) ++
    Seq("v:subject" -> subject)

}

object MailMessage{
  import io.circe.syntax._
  import io.circe._, io.circe.syntax._
  def apply(from:String, to:String, cc:List[String], subject:String, body:String):MailMessage = MailMessage(from, to :: Nil, cc, Nil, subject, body, None, None)
  def matchIdJson(matchId:String) = Some(Json.obj("matchId" -> matchId.asJson))
}

sealed trait MailReceipt{
  def isAccepted:Boolean
}
case class MailAccepted(message:String) extends MailReceipt {
  val isAccepted = true
}
case class MailRejected(message:String, errorCode:Int) extends MailReceipt {
  val isAccepted = false
}
case class OrderConfirmationMail(m: MatchTemplate, config:Config, baseUrl:String, matchId:String){
  def subject = "Bekreftelse på mottatt dommerbestilling"
  def to:Seq[String] = m.clubContact.email :: Nil
  def text =
    s"""
      |Vi bekrefter å ha mottatt din bestilling på følgende kamp:
      |
      |${m.teams}
      |${m.venue}
      |${m.kickoff.toString("dd.MM.yyyy HH:mm")}
      |
      |Kampen vil snart legges ut på ${baseUrl+"/matches"} der våre medlemmer kan melde interesse.
      |Noen dager før kampen vil vi oppnevne dommere som i best mulig grad matcher kampen for både lag og dommere. Dere vil da få beskjed fra oss.
      |
      |Takk for at dere benytter ${config.name}
    """.stripMargin

  def toMailMessage : MailMessage = {
    MailMessage(config.email.fromFdl, to, Seq.empty, Seq.empty, subject, text, None, Some(matchId))
  }
}

case class NewMatchMail(m: MatchTemplate, matchUrl:String, config:Config, matchId:String){
  def toMailMessage = {
    import m._
    MailMessage(
      config.email.fromFdl,
      Seq(config.email.toOnOrders),
      config.email.ccOnOrders,
      Seq.empty,
      "Bestilling av dommer",
      s"""Det er bestilt $refereeType til følgende kamp:
         |${kickoff.toString("dd.MM.yyyy HH:mm")}
         |$teams (${Level.asMap(m.level).toString})
         |$venue
         |Regningen betales av ${m.betalendeLag}
         |Regning sendes til ${m.payerEmail}
         |Gå til $matchUrl for å publisere kampen og se mer informasjon om kampen.
        """.stripMargin,
      None, Some(matchId))
  }
}

case class AppointmentMail(m:Match, config:Config, baseUrl:String, ref:Option[User], ass1:Option[User], ass2:Option[User]){
  def subject = "Dommer tildelt oppdrag"
  def to:Seq[String] = ref.map(_.email).toSeq ++ ass1.map(_.email).toSeq ++ ass2.map(_.email).toSeq
  def text =
    """
      |NB! Bekreft at du aksepterer oppdraget ved å svare på denne mailen!
      |
      |Kan du ikke dømme kampen må du IKKE bytte kampen selv, men bare svare på denne e-posten og gi beskjed om at du ikke kan
      |
      |Dommer tildelt oppdrag
      |%s - %s
      |%s
      |%s
      |Dommer: %s %s (%s kroner)
      |Assistentdommer: %s %s (%s kroner)
      |Assistentdommer: %s %s (%s kroner)
    """.stripMargin.format(m.homeTeam, m.awayTeam,
          m.venue,
          m.kickoffDateTimeString,
          m.appointedRef.map(_.name).getOrElse("Ikke oppnevnt"), formatedPhoneNumberOrBlank(ref), m.refFee.getOrElse("-"),
          m.appointedAssistant1.map(_.name).getOrElse("Ikke oppnevnt"), formatedPhoneNumberOrBlank(ass1), m.assistantFee.getOrElse("-"),
          m.appointedAssistant2.map(_.name).getOrElse("Ikke oppnevnt"), formatedPhoneNumberOrBlank(ass2) ,m.assistantFee.getOrElse("-")
    )

  def html = {
     <h2>Dommer tildelt oppdrag</h2>
      <strong>
        <p>NB! Bekreft at du aksepterer oppdraget ved å svare på denne mailen!</p>
        <p>Kan du ikke dømme kampen må du <em>IKKE bytte kampen selv</em>, men bare svare på denne e-posten og gi beskjed om at du ikke kan</p>
      </strong>
      <table style="text-align: left; float: left;">
        <tr>
          <th>Kamp</th>
          <td>{m.homeTeam} - {m.awayTeam}</td>
        </tr>
        <tr>
          <th>Bane</th>
          <td>{m.venue}</td>
        </tr>
        <tr>
          <th>Avspark</th>
          <td>{m.kickoffDateTimeString}</td>
        </tr>
        <tr>
          <th>Honorar dommer</th>
          <td>{m.refFee.getOrElse("-")}</td>
        </tr>
        <tr>
          <th>Honorar Assistentdommer</th>
          <td>{m.assistantFee.getOrElse("-")}</td>
        </tr>
        <tr>
          <th>Dommerregning betales av</th>
          <td>{m.betalendeLag}</td>
        </tr>
        <tr>
          <th>Dommerregning sendes til</th>
          <td>{m.dommerregningSendesTil}</td>
        </tr>
        <tr>
          <th>Dommer</th>
          <td>{m.appointedRef.map(_.name).getOrElse("Ikke oppnevnt") + formatedPhoneNumberOrBlank(ref)}</td>
        </tr>
        {if(m.refereeType==RefereeType.Trio.key)(
        <tr>
          <th>Assistentdommer 1</th>
          <td>{m.appointedAssistant1.map(_.name).getOrElse("Ikke oppnevnt") + formatedPhoneNumberOrBlank(ass1)}</td>
        </tr>
        <tr>
          <th>Assistentdommer 2</th>
          <td>{m.appointedAssistant2.map(_.name).getOrElse("Ikke oppnevnt")+ formatedPhoneNumberOrBlank(ass2)}</td>
        </tr>)
        }
      </table>
  }

  def formatedPhoneNumberOrBlank(u:Option[User]) = u.map(" (Tel: " + _.telephone + ")").getOrElse("")

  def toMailMessage = {
    val to = ref.map(_.email).toSeq ++ ass1.map(_.email).toSeq ++ ass2.map(_.email).toSeq
    MailMessage(config.email.fromFdl, to, Nil, Nil, subject, text, Some(html), Some(m.id.get.toString))
  }

}

case class ClubRefereeNotification(m:Match, config: Config, ref:Option[User], ass1:Option[User], ass2:Option[User]){
  def to = m.clubContact.map(_.email)
  def cc = config.email.toOnOrders :: Nil
  def text = {
    s"""
       |Det er blitt satt opp dommere til følgende kamp:
       |${m.teams}
       |${m.venue}
       |${m.kickoffDateTimeString}
       |Dommer: ${m.appointedRef.map(_.name).getOrElse("")}${ref.map(", " + _.telephone).getOrElse("")}${ref.flatMap(_ => m.refFee.map(h => s", $h kroner")).getOrElse("")}
       |${if(m.refereeType == RefereeType.Trio.key)
          s"""|AD1: ${m.appointedAssistant1.map(_.name).getOrElse("Ikke oppnevnt")}${ass1.map(", "+_.telephone).getOrElse("")}${ass1.flatMap(_ => m.assistantFee.map(h => s", $h kroner")).getOrElse("")}
              |AD2: ${m.appointedAssistant2.map(_.name).getOrElse("Ikke oppnevnt")}${ass2.map(", "+_.telephone).getOrElse("")}${ass2.flatMap(_ => m.assistantFee.map(h => s", $h kroner")).getOrElse("")}
           """.stripMargin
    }
    |${config.clubNotificationFooter.getOrElse("")}
    |God kamp!
     """.stripMargin
  }
  def html : NodeSeq = {
    <div>
    <h2>Dommeroppsett</h2>
      <p>
        {config.name}
        har satt opp dommer for kamp</p>
      <table style="text-align: left;">
        <tr>
          <th>Kamp</th>
          <td>
            {m.homeTeam}
            -
            {m.awayTeam}
          </td>
        </tr>
        <tr>
          <th>Bane</th>
          <td>
            {m.venue}
          </td>
        </tr>
        <tr>
          <th>Avspark</th>
          <td>
            {m.kickoffDateTimeString}
          </td>
        </tr>
        <tr>
          <th>Dommerregning sendes til</th>
          <td>
            {m.payerEmail}
          </td>
        </tr>
        <tr>
          <th>Dommer</th>
          <td>
            {m.appointedRef.map(r => s"${r.name} (Tel: ${ref.get.telephone}) (Honorar: ${m.refFee.get} kroner)").getOrElse("Ikke oppnevnt")}
          </td>
        </tr>{if (m.refereeType == RefereeType.Trio.key) (
        <tr>
          <th>Assistentdommer 1</th>
          <td>
            {m.appointedAssistant1.map(r => s"${r.name} (Tel: ${ass1.get.telephone}) (Honorar: ${m.assistantFee.get} kroner)").getOrElse("Ikke oppnevnt")}
          </td>
        </tr>
          <tr>
            <th>Assistentdommer 2</th>
            <td>
            {m.appointedAssistant2.map(r => s"${r.name} (Tel: ${ass2.get.telephone}) (Honorar: ${m.assistantFee.get} kroner)").getOrElse("Ikke oppnevnt")}
            </td>
          </tr>)}
      </table>
      <p></p>
      <div>
        <p>
          {config.clubNotificationFooter.getOrElse("")}
        </p>
        <p>
          God kamp!
        </p>
      </div>
    </div>
    }

    def toMailMessage: MailMessage = {
      MailMessage(config.email.fromFdl,
        m.clubContact.map(_.email).getOrElse(config.email.fromFdl) :: Nil,
        cc,
        Nil,
        "Dommeroppsett klart",
        text, Some(html), m.id.map(_.toString))
    }

}
