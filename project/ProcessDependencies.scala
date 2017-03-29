import sbt._
import Keys._

object ProcessDependencies{
  val stanfordNLP = "edu.stanford.nlp"    % "stanford-corenlp" % "3.6.0"

  val jsoup = "org.jsoup"   %   "jsoup"   % "1.8.3"

  val selenium = "org.seleniumhq.selenium" % "selenium-java" % "2.53.0"

  //val scrimageVersion = "2.1.5"
  //val scrimage = Seq(
  //  "scrimage-core", "scrimage-io-extra", "scrimage-filters").
  //  map("com.sksamuel.scrimage" %% _ % scrimageVersion)

  val scalr  = "org.imgscalr" % "imgscalr-lib" % "4.2"

//  val openimajCore = "org.openimaj" % "core-image" % "1.3.5"
//  val openimajFace =  "org.openimaj" % "faces" % "1.3.5"
//  val openimajFeatures =   "org.openimaj" % "image-local-features" % "1.3.5"

  val aliyunOSS = "com.aliyun.oss" % "aliyun-sdk-oss" % "2.2.3"

  val processDependencies: Seq[ModuleID] = Seq(stanfordNLP, jsoup, selenium, scalr, aliyunOSS)
}
