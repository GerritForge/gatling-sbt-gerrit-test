enablePlugins(GatlingPlugin)

scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

val gatlingVer = "3.2.0"

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVer % Test,
  "io.gatling" % "gatling-test-framework" % gatlingVer % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "com.github.pureconfig" %% "pureconfig" % "0.11.1",
  "com.gerritforge" %% "gatling-git" % "1.0.10-8-ga5dc7e9"  excludeAll(
    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers += Resolver.sonatypeRepo("snapshots")
