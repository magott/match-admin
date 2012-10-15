import java.util.Locale
import unfiltered.jetty
import util.Properties
import web.Resources

object Jetty extends App{

  Locale.setDefault(new Locale("no_NO"))
  val port = Properties.envOrElse("PORT", "8080").toInt
  println("Starting on port:" + port)
  val http = jetty.Http(port)
  http.resources(getClass().getResource("/static")).plan(new Resources)
  .run()

}
