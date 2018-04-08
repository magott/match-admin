package service

import java.time.LocalDateTime

import cats.effect.IO
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import data.{MailDelivered, MatchEvent, OkLevel}
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import doobie._
import doobie.implicits._
import io.circe.Json


/**
  *
  */
class DbTest extends FunSuite with BeforeAndAfterAll {

  private var postgres: EmbeddedPostgres = _

  override def beforeAll(): Unit = {
    postgres = db.Db.postgres
    val flyway = new Flyway
    flyway.setDataSource(postgres.getPostgresDatabase)
    flyway.clean()
    flyway.migrate()
  }

  lazy val transactor = Transactor.fromDataSource[IO](postgres.getPostgresDatabase)

  override def afterAll(): Unit = {
    postgres.close()
  }
  val matchEvent = MatchEvent(None, "morten@andersen-gott.com", "124", "uuid", LocalDateTime.now(), "desc", Json.fromString("details"), MailDelivered, OkLevel, Some("morten@example.com"))

  test("foo"){
    val id = DbRepo.insertMatchEvent(matchEvent).withUniqueGeneratedKeys[Int]("id").transact(transactor)
    println(id.unsafeRunSync())
  }

}
