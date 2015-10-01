import com.typesafe.sbt.SbtStartScript

SbtStartScript.startScriptForClassesSettings

name:="match-admin"

scalaVersion:="2.11.2"

organization := "com.andersen-gott"

libraryDependencies ++=
  Seq(
    "net.databinder" %% "unfiltered" % "0.8.1",
    "net.databinder" %% "unfiltered-filter" % "0.8.1",
    "net.databinder" %% "unfiltered-jetty" % "0.8.1",
    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "2.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "2.7.3" excludeAll(ExclusionRule(organization = "org.slf4j")),
    "org.mongodb" % "mongo-java-driver" % "2.13.2",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "org.constretto" %% "constretto-scala" % "1.1",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )