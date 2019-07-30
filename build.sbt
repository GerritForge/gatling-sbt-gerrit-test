enablePlugins(GatlingPlugin)

scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

val gatlingVer = "3.2.0"

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVer,
  "io.gatling" % "gatling-test-framework" % gatlingVer,
  "com.github.pureconfig" %% "pureconfig" % "0.11.1",
  "org.scalatest" %% "scalatest" % "3.0.8",
  "com.gerritforge" %% "gatling-git" % "1.0.2-13-g6142057"
).map(_ % "test,it")
