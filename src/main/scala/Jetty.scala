import java.util.Locale
import org.joda.time.DateTime
import unfiltered.jetty
import web.{UserUpdateInterceptor, Resources}

import scala.util.Properties

object Jetty extends App{

  Locale.setDefault(new Locale("no_NO"))
  System.setProperty("user.timezone", "Europe/Oslo")
  val port = Properties.envOrElse("PORT", "1234").toInt
  val checkUserLevelSet = Properties.envOrElse("CHECK_USERS", "false").toBoolean
  val userUpdateLimit = DateTime.parse(Properties.envOrElse("USER_UPDATE_DATE", DateTime.now.toString))
  println("Starting on port:" + port)
  val http = jetty.Http(port)
  http.resources(getClass().getResource("/static"))
    .plan(new UserUpdateInterceptor(userUpdateLimit, checkUserLevelSet))
    .plan(new Resources)
  .run()

}
