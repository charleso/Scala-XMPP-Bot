import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.drashid"
  val buildVersion      = "2.0.29"
  val buildScalaVersion = "2.9.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion
  )
}

object Resolvers {
  val akkaRepo = "Akka Repo" at "http://repo.akka.io/releases"
  //val gfRepo = "Guiceyfruit Release Repo" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
}

object Dependencies {
  val akka_version = "1.1.3"
  val smack = "jivesoftware" % "smack" % "3.1.0"
  val smackx = "jivesoftware" % "smackx" % "3.1.0"
  val akka_actor = "se.scalablesolutions.akka" % "akka-actor" % akka_version
  //val akka_spring = "se.scalablesolutions.akka" % "akka-spring" % akka_version
  val http = "net.databinder" %% "dispatch-http" % "0.8.5"
  val json = "net.liftweb" %% "lift-json" % "2.4"
}

object BuildSetup extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  val deps = Seq(
    smack, smackx, akka_actor, http, json
  )

  val res = Seq(
    akkaRepo
  )

  lazy val project = Project(
    "scala-bot",
    file("."),
    settings = buildSettings ++ Seq(libraryDependencies ++= deps, resolvers := res)
  )
}
