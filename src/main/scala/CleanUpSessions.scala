import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.Imports._
import common.MongoSetting
import org.joda.time.DateTime

import scala.util.Properties

object CleanUpSessions {

  def cleanup() {
    RegisterJodaTimeConversionHelpers()

    val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")

    println("Hello, I'm cleaning old sessions")

    val affected = db("sessions").remove(("expires" $lt DateTime.now)).getN

    println("%s stale sessions deleted".format(affected))
  }
}
