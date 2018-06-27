import java.time.LocalDateTime

import io.circe.Encoder
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
  *
  */
package object data {
  implicit val encodeLdt : Encoder[LocalDateTime] = Encoder.encodeString.contramap(_.toString)

  implicit class ObjectIdPimp(objectId: ObjectId){
    def dateTime = new DateTime(objectId.getDate)
  }
}
