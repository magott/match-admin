package service

import java.sql.Timestamp
import java.time.LocalDateTime

import cats.effect.IO
import data._
import doobie._
import doobie.implicits._
import cats.implicits._
import io.circe._
import org.postgresql.util.PGobject


/**
  *
  */
object DbRepo {

  implicit val LocalDateTimeMeta: Meta[LocalDateTime] = Meta[java.sql.Timestamp].xmap(
    ts => ts.toLocalDateTime,
    ldt =>Timestamp.valueOf(ldt)
  )

  implicit val MetaJson: Meta[Json] = Meta.other[PGobject]("jsonb").xmap[Json](
    pg => parser.parse(pg.getValue).fold(throw _, identity),
    j => {
      val pg = new PGobject()
      pg.setType("jsonb")
      pg.setValue(j.noSpaces)
      pg
    }
  )

  case class Person(name:String, age:Int)

  val selectMatchEvent = fr"select id, usr, matchid, uuid, timestamp, description, details, eventtype, eventlevel, recipient from match_event"
  def whereRecipient(recipient:String) = fr"where recipient = $recipient"
  def whereMatchId(matchid:String) = fr"where matchid = $matchid"

  val name:Query0[Person] = sql"select name, age from x".query[Person]
  def params(p:Int) = sql"select name from x where p < $p".query[String]

  def insertMatchEvent(e:MatchEvent):Update0 =
    sql"""insert into match_event(usr, matchid, uuid, timestamp, description, details, eventtype, eventlevel, recipient)
         values(${e.endretAv}, ${e.matchId}, ${e.uuid}, ${e.timestamp},${e.description},${e.details},${e.typ},${e.level},${e.recipient})""".update

  def insertMatchEvent2:Update[MatchEvent] =
    Update("""insert into match_event(usr, matchid, uuid, timestamp, description, details, eventtype, eventlevel, recipient)
         values(?,?,?,?,?,?,?,?,?)""")

  def eventsByMatch(matchid:String):Query0[MatchEvent] =
    (selectMatchEvent ++ whereMatchId(matchid)).query[MatchEvent]
}

case class DbRepo(tx:Transactor[IO]) {
  def name = DbRepo.name.to[Vector].transact(tx)

  def eventsByMatch(matchId:String) = DbRepo.eventsByMatch(matchId).to[List].transact(tx).unsafeRunSync()

  def insert(e:MatchEvent) = DbRepo.insertMatchEvent(e).run.transact(tx).unsafeRunSync()

  def insertMany(es:List[MatchEvent]) = DbRepo.insertMatchEvent2.updateMany(es).transact(tx)

}
