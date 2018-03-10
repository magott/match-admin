import java.util.Locale

import cats.effect.Async
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import conf.Config
import doobie.hikari.HikariTransactor
import doobie._
import org.constretto.Constretto
import org.constretto.Constretto._
import org.joda.time.DateTime
import unfiltered.jetty
import web.{Resources, UserUpdateInterceptor}

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

  def hikariTx[F[_] : Async]:Transactor[F] = {
    val dbconfig = new HikariConfig();
    dbconfig.setJdbcUrl("postgres://cdiiurngpxtcdt:751cdc101da712227c9b2e8c4098438ef0511d47ef178b75462bc7ee5b539a5f@ec2-54-75-248-193.eu-west-1.compute.amazonaws.com:5432/dgmqrsmqscfr5")
    val dataSource = new HikariDataSource(dbconfig)
    HikariTransactor[F](dataSource)
  }

}
