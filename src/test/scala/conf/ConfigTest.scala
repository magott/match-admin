package conf

import org.scalatest.{Matchers, FlatSpec}
import org.constretto.{Constretto, Converter}, Constretto._
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

}
