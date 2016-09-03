import sbt._
import Keys._

object CommonDependencies{

  val specsVersion = "3.6.6"
  val specs = Seq(
    "specs2-core", "specs2-junit", "specs2-mock").
    map("org.specs2" %% _ % specsVersion % Test)

  val scalacheck = Seq(
    "org.specs2"     %% "specs2-scalacheck" % specsVersion % Test,
    "org.scalacheck" %% "scalacheck"        % "1.13.0"     % Test)

  val jodaTime = "joda-time"  % "joda-time" % "2.8.2"

  val logback = "ch.qos.logback"    %   "logback-classic"  % "1.1.3"

  val codec = "commons-codec"   % "commons-codec"   % "1.10"

  val PlayJson = "com.typesafe.play" % "play-json_2.11" % "2.5.2"

  val Mail = "me.lessis" %% "courier" % "0.1.3"

  val BetterFile = "com.github.pathikrit" % "better-files_2.11" % "2.16.0"

  val asynchttpclient = "org.asynchttpclient" % "async-http-client" % "2.0.14"

  val commonDependencies: Seq[ModuleID] = specs ++ scalacheck ++ Seq(jodaTime,logback,codec,PlayJson,Mail,BetterFile,asynchttpclient)
}