import sbt._
import Keys._

object Dependencies {

  val commonDependencies = CommonDependencies.commonDependencies

  val databaseDependencies = DatabaseDependencies.databaseDependencies

  val playDependencies = PlayDependencies.playDependencies

  val akkaDependencies = AkkaDependencies.akkaDependencies

  val processDependencies = ProcessDependencies.processDependencies
}