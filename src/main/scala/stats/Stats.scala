package stats

import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import common.MongoSetting
import data.Level.{Men1Div, MenPrem}
import data.{Level, RefereeType, Referee}
import org.joda.time.DateTime
import service.MongoRepository

import scala.util.Properties

/**
 *
 */
object Stats extends App{

  RegisterJodaTimeConversionHelpers()

  private val mongolab = Properties.envOrNone("MONGOLAB_URI")

  private val MongoSetting(db) = mongolab

  private val startOfSeason = DateTime.now().withTimeAtStartOfDay().withDayOfMonth(1).withMonthOfYear(11).minusYears(1)

  private val matchesThisYear = new MongoRepository(db).listPublishedMatchesNewerThan(startOfSeason)

  val refstats: Map[Referee, List[RefStat]] = matchesThisYear.foldLeft(Map.empty[Referee, List[RefStat]].withDefaultValue(List.empty[RefStat]))(assignmentsPerReferee)

  val numberOfAssignments = refstats.mapValues(_.size).values.sum
  val numberOfAd = refstats.values.flatten.filter(_.role == Assistant).size
  val numberOfRefsWithMultipleMatches = refstats.filter(_._2.size > 1).keySet.size
  val numberOfRefsWithAssignment = refstats.keySet.size
  val interestedRefs = matchesThisYear.flatMap(m => m.interestedRefs ++ m.interestedAssistants).map(_.id.toString)
  val mostInterestFromReferees = matchesThisYear.maxBy(_.interestedRefs.size)
  val mostInterestFromAd = matchesThisYear.maxBy(_.interestedAssistants.size)
  val differentLevels = matchesThisYear.map(_.level).toSet.size
  val refsAtLevelOrHigher = refstats.filter{ tup =>
    val refAssignments = tup._2.filter(_.role == Referee)
    refAssignments.exists(a => ratedAs(a.level) > ratedAs(tup._1.level))
  }
  val matchesWithRefAtLevelOrHigher = matchesThisYear.filter(m => m.appointedRef.exists(r => ratedAs(r.level) <= ratedAs(m.level))).size
  println(s"Siden $startOfSeason")
  println(s"${matchesThisYear.size} kamper fordelt på $differentLevels forskjellige nivåer")
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

  private def assignmentsPerReferee(assignments: Map[Referee, List[RefStat]], m:data.Match) : Map[Referee, List[RefStat]] = {
    val withReferee = if(m.appointedRef.isDefined) {
      val referee = m.appointedRef.get
      val oldRefStats: List[RefStat] = assignments(referee)
      val newRefStat = RefStat(Referee, m.level) :: oldRefStats
      assignments + (referee -> newRefStat)
    } else assignments
    val withAd1 = if(m.appointedAssistant1.isDefined){
      val referee = m.appointedAssistant1.get
      val oldRefStats:List[RefStat] = withReferee(referee)
      val newRefStat = RefStat(Assistant, m.level) :: oldRefStats
      withReferee + (referee -> newRefStat)
    } else withReferee
    val withAd2 = if(m.appointedAssistant2.isDefined){
      val referee = m.appointedAssistant2.get
      val oldRefStats:List[RefStat] = withAd1(referee)
      val newRefStat = RefStat(Assistant, m.level) :: oldRefStats
      withAd1 + (referee -> newRefStat)
    } else withAd1
    withAd2
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
