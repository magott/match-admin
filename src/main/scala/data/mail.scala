package data

import xml.NodeSeq

case class MailMessage(from:String, to:Seq[String], cc:Seq[String], bcc:Seq[String], subject:String, body:String, bodyHtml:Option[NodeSeq]) {

  def asMailgunParams = Seq("from" -> from, "subject" -> subject, "text"->body) ++ to.map("to"-> _) ++ cc.map("cc" -> _) ++ bcc.map("bcc" -> _) ++ bodyHtml.map("html" -> _.toString)

}

object MailMessage{
  def apply(from:String, to:String, cc:List[String], subject:String, body:String):MailMessage = MailMessage(from, to :: Nil, cc, Nil, subject, body, None)
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

case class AppointmentMail(m:Match, cc:List[String], baseUrl:String, ref:Option[User], ass1:Option[User], ass2:Option[User]){
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
}
