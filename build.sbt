name := "BDP-Root"

version := "1.0"

scalaVersion := "2.11.8"

lazy val common = project.
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= Dependencies.databaseDependencies).
  settings(libraryDependencies ++= Dependencies.commonDependencies)

lazy val webserver = project.
  dependsOn(common).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= Seq(specs2,filters,evolutions)).
  settings(libraryDependencies ++= Dependencies.akkaDependencies).
  settings(libraryDependencies ++= Dependencies.playDependencies).
  enablePlugins(PlayScala)

lazy val proserver = project.
  dependsOn(common).settings(Commons.settings: _*).
  settings(libraryDependencies ++= Dependencies.akkaDependencies).
  settings(libraryDependencies ++= Dependencies.processDependencies).
  settings(Commons.commonAssemblyMergeStrategy)

lazy val dispatcher = project.
  dependsOn(common).settings(Commons.settings: _*).
  settings(libraryDependencies ++= Dependencies.akkaDependencies).
  settings(Commons.commonAssemblyMergeStrategy)