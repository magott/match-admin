enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("matchadmin.Jetty")

name:="match-admin"

scalaVersion:="2.12.7"

organization := "com.andersen-gott"

val doobieVersion = "0.5.0"

libraryDependencies ++=
  Seq(
    "org.scala-lang.modules"     %% "scala-xml"                 % "1.1.1",
    "org.flywaydb"               %  "flyway-core"               % "5.0.7",
    "org.postgresql"             %  "postgresql"                % "42.2.1",
    "com.zaxxer"                 %  "HikariCP"                  % "2.7.7",
    "org.tpolecat"               %% "doobie-core"               % doobieVersion,
    "org.tpolecat"               %% "doobie-hikari"             % doobieVersion, // HikariCP transactor.
    "org.tpolecat"               %% "doobie-postgres"           % doobieVersion,
    "ws.unfiltered"              %% "unfiltered"                % "0.9.1",
    "ws.unfiltered"              %% "unfiltered-filter"         % "0.9.1",
    "ws.unfiltered"              %% "unfiltered-jetty"          % "0.9.1",
    "ws.unfiltered"              %% "unfiltered-filter-uploads" % "0.9.1",
    "org.scalaj"                 %% "scalaj-http"               % "2.4.0",
    "commons-logging"            % "commons-logging"            % "1.1.1",
    "joda-time"                  % "joda-time"                  % "2.1",
    "org.mindrot"                % "jbcrypt"                    % "0.4",
    "org.joda"                   % "joda-convert"               % "1.2",
    "com.google.guava"           % "guava"                      % "11.0.2",
    "org.mongodb"                %% "casbah"                    % "3.1.1"   excludeAll ExclusionRule(organization = "org.slf4j"),
//    "org.mongodb" % "mongo-java-driver" % "2.13.2",
    "org.scalaz"                 %% "scalaz-core"               % "7.1.15",
    "net.databinder.dispatch"    %% "dispatch-core"             % "0.13.3",
    "org.constretto"             %% "constretto-scala"          % "1.2",
    "ch.qos.logback"             % "logback-classic"            % "1.1.2",
    "io.circe"                   %% "circe-core"                % "0.9.0",
    "io.circe"                   %% "circe-generic"             % "0.9.0",
    "io.circe"                   %% "circe-parser"              % "0.9.0",
    "io.circe"                   %% "circe-literal"             % "0.9.0",
    "org.scalatest"              %% "scalatest"                 % "3.0.5"       % "test",
    "com.opentable.components"   % "otj-pg-embedded"            % "0.11.4"      % "test"

  )