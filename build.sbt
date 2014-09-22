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
    "org.slf4j" % "slf4j-simple" % "1.6.4",
    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "2.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.joda" % "joda-convert" % "1.2",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "com.google.guava" % "guava" % "11.0.2",
    "org.mongodb" %% "casbah" % "2.7.3",
    "org.mongodb" % "mongo-java-driver" % "2.12.3",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "org.constretto" %% "constretto-scala" % "1.1"
  )