import sbt._
import Keys._

object DatabaseDependencies{

  val slickVersion = "3.1.1"
  val slick = Seq("slick","slick-hikaricp").map("com.typesafe.slick" %% _ % slickVersion)

  val postgresqlVersion = "9.4-1204-jdbc41"
  val postgresql = "org.postgresql"      % "postgresql"      % postgresqlVersion

  val slickPgVersion = "0.13.0"
  val slickPg = Seq(
    "slick-pg","slick-pg_joda-time","slick-pg_json4s","slick-pg_play-json").
    map("com.github.tminglei" %% _ % slickPgVersion)

  val jodaConvert = "org.joda" % "joda-convert" % "1.7"

  val rediscala = "com.github.etaty" %% "rediscala" % "1.6.0"

  val reactiveKafka = "com.softwaremill.reactivekafka" %% "reactive-kafka-core" % "0.10.0"

  val elastic4s = "com.sksamuel.elastic4s" % "elastic4s-core_2.11" % "2.3.0"

  val databaseDependencies: Seq[ModuleID] = slick ++ slickPg ++ Seq(postgresql,rediscala,jodaConvert,elastic4s)
}