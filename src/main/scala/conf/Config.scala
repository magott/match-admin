package conf

import org.constretto.Converter._

/**
 *
 */
case class Config(tag:String, name:String, css:String, heading:String, image:String, refNoPrefix:String, email:EmailConfig, javascript:String, favicon:String) {

}

case class EmailConfig(fromFdl:String, toOnOrders:String, ccOnOrders:List[String], mailgunAppName: Option[String], clubNotificationFooter: Option[String])

object EmailConfig{
  implicit val emailConfigC = fromObject{obj =>
    EmailConfig(
      obj[String]("fromFdl"),
      obj[String]("toOnOrders"),
      obj[List[String]]("ccOnOrders"),
      obj.get[String]("mailgunAppName"),
      obj.get[String]("clubNotificationFooter")
    )
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