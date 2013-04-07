import data.{MatchTemplate, Level}
import org.joda.time.DateTime
import org.scalatest.FunSuite

class PlaygroundTest extends FunSuite{

  test("Pattern match"){
    List("hello", "is","it", "me") match{
      case List("hello", is, _*) => println("match1")
      case _ => println("match2")
    }
  }

}
