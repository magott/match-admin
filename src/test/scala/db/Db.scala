package db

import java.io.File

import com.opentable.db.postgres.embedded.EmbeddedPostgres

/**
  *
  */
object Db {

  val postgres = EmbeddedPostgres
    .builder()
    .setDataDirectory(new File("target/db"))
    .setCleanDataDirectory(false)
    .start()

}
