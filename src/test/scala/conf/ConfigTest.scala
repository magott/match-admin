package conf

import org.scalatest.{Matchers, FlatSpec}
import org.constretto.{Constretto, Converter}, Constretto._
import org.scalatest.OptionValues._

/**
 *
 */
class ConfigTest extends FlatSpec with Matchers{

  "Ofdl config" should "be parsable" in {
    System.setProperty("CONSTRETTO_TAGS", "ofdl")
    val c = Constretto(
      List(
        json("classpath:conf/ofdl.conf", "config", Some("ofdl"))
      )
    )
    val config = c[Config]("config")
    config.email.ccOnOrders.length shouldBe (2)
  }

  "Tfdl config" should "be parsable" in {
    System.setProperty("CONSTRETTO_TAGS", "tfdl")
    val c = Constretto(
      List(
        json("classpath:conf/tfdl.conf", "config", Some("tfdl"))
      )
    )
    val config = c[Config]("config")
    config.email.ccOnOrders shouldBe empty
    config.email.mailgunAppName.value shouldBe ("tfdl.no")

  }

  "Dev config" should "be parsable" in {
    System.setProperty("CONSTRETTO_TAGS", "dev")
    val c = Constretto(
      List(
        json("classpath:conf/dev.conf", "config", Some("dev"))
      )
    )
    val config = c[Config]("config")
    config.email.ccOnOrders.length shouldBe (2)
    config.email.mailgunAppName.value shouldBe ("andersen-gott.com")
  }

}
