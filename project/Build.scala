import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.drashid"
  val buildVersion      = "2.0.29"
  val buildScalaVersion = "2.9.0"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val akkaRepo = "Akka Repo" at "http://akka.io/repository"
  val gfRepo = "Guiceyfruit Release Repo" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
}

object Dependencies {
  val akka_version = "1.1.3"
  val xmpp = "jivesoftware" % "smack" % "3.1.0"
  val akka_actor = "se.scalablesolutions.akka" % "akka-actor" % akka_version
  val akka_spring = "se.scalablesolutions.akka" % "akka-spring" % akka_version
}

object BuildSetup extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val deps = Seq(
    xmpp, akka_actor, akka_spring
  )

  val res = Seq(
    akkaRepo, gfRepo
  )

  lazy val project = Project(
    "scala-bot",
    file("."),
    settings = buildSettings ++ Seq(libraryDependencies ++= deps, resolvers := res)
  )
}