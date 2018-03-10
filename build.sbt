import com.typesafe.sbt.SbtStartScript

SbtStartScript.startScriptForClassesSettings

name:="match-admin"

scalaVersion:="2.11.12"

organization := "com.andersen-gott"

val doobieVersion = "0.5.0"

libraryDependencies ++=
  Seq(
    "org.flywaydb"      %  "flyway-core"        % "5.0.7",
    "org.postgresql"    %  "postgresql"         % "42.2.1",
    "com.zaxxer"        %  "HikariCP"           % "2.7.7",
    "org.tpolecat"      %% "doobie-core"        % doobieVersion,
    "org.tpolecat"      %% "doobie-hikari"      % doobieVersion, // HikariCP transactor.
    "org.tpolecat"      %% "doobie-postgres"    % doobieVersion,
    "net.databinder" %% "unfiltered" % "0.8.1",
    "net.databinder" %% "unfiltered-filter" % "0.8.1",
    "net.databinder" %% "unfiltered-jetty" % "0.8.1",
    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "2.1",
    "org.mindrot" % "jbcrypt" % "0.4",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "3.1.1" excludeAll(ExclusionRule(organization = "org.slf4j")),
//    "org.mongodb" % "mongo-java-driver" % "2.13.2",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "org.constretto" %% "constretto-scala" % "1.1",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "io.circe" %% "circe-core" % "0.9.0",
    "io.circe" %% "circe-parser" % "0.9.0",
    "io.circe" %% "circe-literal" % "0.9.0"

  )