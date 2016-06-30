import sbt._
import Keys._

object PlayDependencies{

  val playVersion = "2.5.2"
  val play = "com.typesafe.play" % "play_2.11" % playVersion
  val playSlick = "com.typesafe.play" %% "play-slick" % "2.0.0"
  val playSlickEvo = "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0"
  val jts = "com.vividsolutions" % "jts" % "1.13"

  val play2AuthVersion = "0.14.2"
  val play2AuthBase = "jp.t2v" %% "play2-auth"          % play2AuthVersion
  val play2AuthSocial = "jp.t2v" %% "play2-auth-social" % play2AuthVersion
  val play2AuthTest = "jp.t2v" %% "play2-auth-test"     % play2AuthVersion  % Test
  val play2Auth = Seq(play2AuthBase,play2AuthSocial,play2AuthTest)

  val playDependencies: Seq[ModuleID] = play2Auth ++ Seq(jts,play,playSlick,playSlickEvo)
}