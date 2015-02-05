import java.text.DecimalFormat

/**
 *
 */
package object common {

  def durationSeconds (start:Long, end:Long) : String = {
    val formatter = new DecimalFormat("0.000")
    val duration = end - start
    val durationS = (duration.toDouble / 1000d)
    s"${formatter.format(durationS)} s"
  }
}
