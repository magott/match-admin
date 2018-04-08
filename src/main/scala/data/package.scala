import java.time.LocalDateTime

import io.circe.Encoder

/**
  *
  */
package object data {
  implicit val encodeLdt : Encoder[LocalDateTime] = Encoder.encodeString.contramap(_.toString)
}
