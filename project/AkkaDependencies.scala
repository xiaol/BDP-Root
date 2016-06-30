import sbt._
import Keys._

object AkkaDependencies{
  val akkaVersion = "2.4.7"
  val akka = Seq(
    "akka-actor","akka-remote","akka-cluster","akka-slf4j","akka-http-core","akka-http-experimental").
    map("com.typesafe.akka" %% _ % akkaVersion)
  val akkaTest = Seq("akka-testkit").
    map("com.typesafe.akka" %% _ % akkaVersion % Test)

  //val Koauth = "com.hunorkovacs" %% "koauth" % "1.1.0" exclude("com.typesafe.akka", "akka-actor_2.11")

  val akkaDependencies: Seq[ModuleID] = akka ++ akkaTest // :+ Koauth
}