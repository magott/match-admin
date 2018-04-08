package matchadmin

import org.flywaydb.core.Flyway


/**
  *
  */
object JettyDev extends App {

  import db.Db._
  private val flyway = new Flyway()

  private val database = postgres.getPostgresDatabase

  flyway.setDataSource(database)
  flyway.clean()
  flyway.migrate()

  Jetty.run(database, () => postgres.close())

}
