import java.util.Locale
import conf.Config
import org.constretto.Constretto
import org.constretto.Constretto._
import org.joda.time.DateTime
import unfiltered.jetty
import web.{UserUpdateInterceptor, Resources}

import scala.util.Properties

object Jetty extends App{

  Locale.setDefault(new Locale("no_NO"))
  System.setProperty("user.timezone", "Europe/Oslo")
  val port = Properties.envOrElse("PORT", "1234").toInt
  val config = getConfig
  val checkUserLevelSet = Properties.envOrElse("CHECK_USERS", "false").toBoolean
  val userUpdateLimit = DateTime.parse(Properties.envOrElse("USER_UPDATE_DATE", DateTime.now.toString))
  println("Starting on port:" + port)
  val http = jetty.Http(port)
  http.resources(getClass().getResource("/static"))
    .plan(new UserUpdateInterceptor(userUpdateLimit, checkUserLevelSet))
    .plan(new Resources(config))
  .run()

  private def getConfig:Config = {
    if (Properties.envOrNone("CONSTRETTO_TAGS").isEmpty && Properties.envOrNone("DATABASE_URL").isDefined) sys.error("Running on Heroku with no CONSTRETTO_TAGS set. Aborting")
    if (Properties.envOrNone("CONSTRETTO_TAGS").isEmpty) Properties.setProp("CONSTRETTO_TAGS","dev")
    val c = Constretto(
      List(
        json("classpath:conf/ofdl.conf", "config", Some("ofdl")),
        json("classpath:conf/tfdl.conf", "config", Some("tfdl")),
        json("classpath:conf/dev.conf", "config", Some("dev"))
      )
    )
    val config = c[Config]("config")
    config
  }

}
