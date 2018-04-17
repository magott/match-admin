package stats


import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import common.MongoSetting
import data.Level.{Men1Div, MenPrem}
import data.{Level, Match, Referee, RefereeType}
import io.circe.Json
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import service.MongoRepository
import stats.Stats.{Assistant, RefStat}

import scala.util.Properties

/**
  *
  */
object Stats{

  def main(array: Array[String]) {
    Stats(MongoRepository.singleton, 2017).print
  }

  def apply(repo: MongoRepository, year:Int): Stats ={
    val from = new LocalDate(year, 11, 11).toDateTimeAtStartOfDay(DateTimeZone.forID("Europe/Oslo"))
    val to = from.plusYears(1)
    val matchesOfYear = repo.listPublishedMatchesBetween(from, to)
    val refstats = matchesOfYear.flatMap(m =>
      m.appointedRef.map(_ -> RefStat(Referee, m.level)).toList ++ m.appointedAssistant1.map(_ -> RefStat(Assistant, m.level)) ++ m.appointedAssistant2.map(_ -> RefStat(Assistant, m.level)))
      .groupBy(_._1).mapValues(_.map(_._2))
    Stats(from, to, matchesOfYear, refstats)
  }

  case class RefStat(role:Role, level:String)

  sealed abstract class Role(role:String)
  case object Referee extends Role("referee")
  case object Assistant extends Role("assistant")

  import data.Level._
  def ranking = Seq(
    MenPrem.display,
    Men1Div.display,
    Men2Div.display,
    WomenPrem.display,
    Men3Div.display,
    Boys19IK.display,
    Women1Div.display,
    Men4Div.display,
    Boys19.display,
    Boys16IK.display,
    Men5Div.display,
    Men6Div.display,
    Men7Div.display,
    Men8Div.display,
    Boys16.display,
    Women2Div.display,
    Women3Div.display,
    Women4Div.display,
    Girls19.display,
    Girls16.display,
    Boys15.display,
    Boys14.display,
    Boys13.display,
    Girls15.display,
    Girls14.display,
    Girls13.display
  )
  def ratedAs(level: String) = {
    val index = ranking.indexOf(Level.asMap(level))
    index
  }

}

case class Stats(from:DateTime, to:DateTime, matchesOfYear:Seq[Match], refstats:Map[Referee, Seq[RefStat]]){
  val numberOfAssignments = refstats.mapValues(_.size).values.sum
  val numberOfAd = refstats.values.flatten.filter(_.role == Assistant).size
  val numberOfRefsWithMultipleMatches = refstats.filter(_._2.size > 1).keySet.size
  val numberOfRefsWithAssignment = refstats.keySet.size
  val interestedRefs = matchesOfYear.flatMap(m => m.interestedRefs ++ m.interestedAssistants).map(_.id.toString)
  val mostInterestFromReferees = matchesOfYear.maxBy(_.interestedRefs.size)
  val mostInterestFromAd = matchesOfYear.maxBy(_.interestedAssistants.size)
  val differentLevels = matchesOfYear.map(_.level).toSet.size
  val refsAtLevelOrHigher = refstats.filter { tup =>
    val refAssignments: Seq[RefStat] = tup._2.filter(_.role == Stats.Referee)
    refAssignments.exists(a => Stats.ratedAs(a.level) >= Stats.ratedAs(tup._1.level))
  }
  val matchesWithRefAtLevelOrHigher = matchesOfYear.filter(m => m.appointedRef.exists(r => Stats.ratedAs(r.level) <= Stats.ratedAs(m.level))).size

  def statsJson : Json = {
    Json.arr(
      Json.fromString(s"Mellom ${from.toString("yyyy-MM-dd")} og ${to.toString("yyyy-MM-dd")}"),
      Json.fromString(s"${matchesOfYear.size} kamper fordelt på $differentLevels forskjellige nivåer"),
      Json.fromString(s"$numberOfAssignments oppdrag totalt"),
      Json.fromString(s"$numberOfAd AD-oppdrag"),
      Json.fromString(s"${interestedRefs.size} ganger har medlemmer meldt interesse for oppdrag"),
      Json.fromString(s"${interestedRefs.toSet.size} forskjellige dommere har meldt interesse for kamp ila sesongen"),
      Json.fromString(s"${numberOfRefsWithAssignment} dommere har fått tildelt kamp ila sesongen"),
      Json.fromString(s"$numberOfRefsWithMultipleMatches dommere har fått mer enn ett oppdrag"),
      Json.fromString(s"${Level.asMap(mostInterestFromAd.level)}-kampen ${mostInterestFromAd.teams} skapte mest interesse blandt AD-er (${mostInterestFromAd.interestedAssistants.size} meldte interesse)"),
      Json.fromString(s"${Level.asMap(mostInterestFromReferees.level)}-kampen ${mostInterestFromReferees.teams} skapte mest interesse blandt dommere (${mostInterestFromReferees.interestedRefs.size} meldte interesse)"),
      Json.fromString(s"${refsAtLevelOrHigher.size} dommere har fått minst ett hoveddommeroppdrag på samme nivå eller høyere enn det de har oppgitt å ha dømt foregående sesong"),
      Json.fromString(s"$matchesWithRefAtLevelOrHigher kamper har hatt hoveddommer som dømmer på samme nivå eller lavere foregående sesong")
    )
  }

  def print = {
    println(s"Mellom $from og $to")
    println(s"${matchesOfYear.size} kamper fordelt på $differentLevels forskjellige nivåer")
    println(s"$numberOfAssignments oppdrag totalt")
    println(s"$numberOfAd AD-oppdrag")
    println(s"${interestedRefs.size} ganger har medlemmer meldt interesse for oppdrag")
    println(s"${interestedRefs.toSet.size} forskjellige dommere har meldt interesse for kamp ila sesongen")
    println(s"${numberOfRefsWithAssignment} dommere har fått tildelt kamp ila sesongen")
    println(s"$numberOfRefsWithMultipleMatches dommere har fått mer enn ett oppdrag")
    println(s"${Level.asMap(mostInterestFromAd.level)}-kampen ${mostInterestFromAd.teams} skapte mest interesse blandt AD-er (${mostInterestFromAd.interestedAssistants.size} meldte interesse)")
    println(s"${Level.asMap(mostInterestFromReferees.level)}-kampen ${mostInterestFromReferees.teams} skapte mest interesse blandt dommere (${mostInterestFromReferees.interestedRefs.size} meldte interesse)")
    println(s"${refsAtLevelOrHigher.size} dommere har fått minst ett hoveddommeroppdrag på samme nivå eller høyere enn det de har oppgitt å ha dømt foregående sesong")
    println(s"$matchesWithRefAtLevelOrHigher kamper har hatt hoveddommer som dømmer på samme nivå eller lavere foregående sesong")

  }

}
