package conf

import org.constretto.Converter._

/**
 *
 */
case class Config(tag:String, name:String, css:String, heading:String, image:String, refNoPrefix:String, email:EmailConfig, javascript:String, favicon:String) {
  def clubNotificationFooter:Option[String]  = EmailConfig.clubNotificationFooter(tag)
}

case class EmailConfig(fromFdl:String, toOnOrders:String, ccOnOrders:List[String], mailgunAppName: Option[String]){
}

object EmailConfig{
  implicit val emailConfigC = fromObject{obj =>
    EmailConfig(
      obj[String]("fromFdl"),
      obj[String]("toOnOrders"),
      obj[List[String]]("ccOnOrders"),
      obj.get[String]("mailgunAppName")
    )
  }
  def clubNotificationFooter(tag: String): Option[String] = {
    tag match {
      case "ofdl" => Some("Dere velger selv om dommerbetaling gjøres kontant til dommerne etter kamp    eller pr. konto. Det bør klareres med dommerne før kampstart hvilket av lagene som skal ta hånd om dommerregningene. Adm-gebyr blir alltid fakturert kontaktperson i ettertid.    Ved avlysning må du ta kontakt på denne mailen innen 48 timer før kampstart, ellers må det betales halvt honorar for kampen. Møter dommerne på banen og kampen blir avlyst, skal helt honorar betales.")
      case "dev" => Some("Dere velger selv om dommerbetaling gjøres kontant til dommerne etter kamp    eller pr. konto. Det bør klareres med dommerne før kampstart hvilket av lagene som skal ta hånd om dommerregningene. Adm-gebyr blir alltid fakturert kontaktperson i ettertid.    Ved avlysning må du ta kontakt på denne mailen innen 48 timer før kampstart, ellers må det betales halvt honorar for kampen. Møter dommerne på banen og kampen blir avlyst, skal helt honorar betales.")
      case _ => None
    }
  }
}

object Config{
  import EmailConfig._
  implicit val configC = fromObject{obj =>
    Config(
    obj[String]("tag"),
    obj[String]("name"),
    obj[String]("css"),
    obj[String]("heading"),
    obj[String]("ogImage"),
    obj[String]("refNoPrefix"),
    obj[EmailConfig]("email"),
    obj[String]("javascript"),
    obj[String]("favicon")
    )
  }
}