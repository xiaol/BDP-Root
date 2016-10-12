import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.PathList
import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, scalariformSettings}

import scalariform.formatter.preferences._

object Commons {

  val customeScalariformSettings = ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 200)
    .setPreference(AlignParameters, true)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)

  val settings: Seq[Def.Setting[_]] = scalariformSettings ++ customeScalariformSettings ++ Seq(
    organization := "com.promisehook.bdp",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8"),
    updateOptions := updateOptions.value.withCachedResolution(true),
    fork in run := true,
    test in assembly := {},
    resolvers += Opts.resolver.mavenLocalFile,
    resolvers ++= Seq(
      DefaultMavenRepository,
      Resolver.defaultLocal,
      Resolver.mavenLocal,
      Resolver.jcenterRepo,
      Classpaths.sbtPluginReleases,
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      "Atlassian Releases" at "https://maven.atlassian.com/public/",
      "Apache Staging" at "https://repository.apache.org/content/repositories/staging/",
      "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
      "softprops-maven" at "http://dl.bintray.com/content/softprops/maven",
      "OpenIMAJ maven releases repository" at "http://maven.openimaj.org",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Eclipse repositories" at "https://repo.eclipse.org/service/local/repositories/egit-releases/content/"
    )
  )

  val commonAssemblyMergeStrategy = assemblyMergeStrategy in assembly := {
    case PathList("org", "ansj", xs @ _*)                   => MergeStrategy.first
    case PathList("org", "joda", xs @ _*)                   => MergeStrategy.first
    case PathList("org", "apache", xs @ _*)                 => MergeStrategy.first
    case PathList("org", "nlpcn", xs @ _*)                  => MergeStrategy.first
    case PathList("org", "w3c", xs @ _*)                    => MergeStrategy.first
    case PathList("org", "xml", xs @ _*)                    => MergeStrategy.first
    case PathList("javax", "xml", xs @ _*)                  => MergeStrategy.first
    case PathList("edu", "stanford", xs @ _*)               => MergeStrategy.first
    case PathList("org", "cyberneko", xs @ _*)              => MergeStrategy.first
    case PathList("org", "xmlpull", xs @ _*)              => MergeStrategy.first
    case PathList("org", "objenesis", xs @ _*)              => MergeStrategy.first
    case PathList("com", "esotericsoftware", xs @ _*)        => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".dic"       => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".data"      => MergeStrategy.first
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
    //  case "application.conf"                             => MergeStrategy.concat
    //  case "unwanted.txt"                                 => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
}