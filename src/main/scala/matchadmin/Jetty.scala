package matchadmin

import java.util.Locale
import javax.sql.DataSource

import cats.effect.Async
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import conf.Config
import doobie._
import org.constretto.Constretto
import org.constretto.Constretto._
import org.flywaydb.core.Flyway
import org.joda.time.DateTime
import unfiltered.jetty
import web.{Resources, UserUpdateInterceptor}

import scala.util.Properties

object Jetty extends App{


  val ds = dataSource
  private val flyway = new Flyway()
  flyway.setDataSource(ds)
  flyway.migrate()
  run(ds, () => ds.close())

  def run(dataSource: DataSource, shutdown: () => Unit = () => Unit) : Unit = {
    val port = Properties.envOrElse("PORT", "1234").toInt
    val config = getConfig
    val checkUserLevelSet = Properties.envOrElse("CHECK_USERS", "false").toBoolean
    val userUpdateLimit = DateTime.parse(Properties.envOrElse("USER_UPDATE_DATE", DateTime.now.toString))
    Locale.setDefault(new Locale("no_NO"))
    System.setProperty("user.timezone", "Europe/Oslo")
    println("Starting on port:" + port)
    val http = jetty.Http(port)
    http.resources(getClass().getResource("/static"))
      .plan(new UserUpdateInterceptor(userUpdateLimit, checkUserLevelSet))
      .plan(new Resources(config, tx(dataSource)))
      .run(_ => Unit, _ => shutdown.apply())
  }


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

  def dataSource :HikariDataSource = {
    val jdbcUrl:String = Properties.envOrNone("DATABASE_URL").get
    val dbconfig = new HikariConfig()
    dbconfig.setJdbcUrl(jdbcUrl)
    new HikariDataSource(dbconfig)
  }

  def tx[F[_] : Async](dataSource: DataSource):Transactor[F] = {
    Transactor.fromDataSource[F](dataSource)
  }

}
