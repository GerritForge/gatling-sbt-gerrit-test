enablePlugins(GatlingPlugin)

scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

val gatlingVer = "3.2.1"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.13.0",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVer % Test,
  "io.gatling" % "gatling-test-framework" % gatlingVer % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "com.gerritforge" %% "gatling-git" % "1.0.11" excludeAll(
    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers += Resolver.sonatypeRepo("snapshots")

enablePlugins(DockerPlugin)

val baseDir = "/Users/lucamilanesio/gerrithub/gatling-sbt-gerrit-test"

dockerfile in docker := {
  val classpath = (managedClasspath in Compile).value
  new Dockerfile {
    from("denvazh/gatling:3.2.1")

    stageFiles(Seq("src/test/resources/logback.xml", "src/test/resources/application.conf").map(new File(_)), "conf/")
    stageFile(new File("src/test/scala/gerritforge"), "simulations/")
    copyRaw("simulations/","/opt/gatling/user-files/simulations/")
    copyRaw("conf/", "/opt/gatling/conf/")
    stageFiles(Seq("id_rsa", "id_rsa.pub", "config").map(new File(_)), ".ssh/")
    addRaw(".ssh/","/root/.ssh/")

    add(classpath.files, "/opt/gatling/lib/")
  }
}

imageNames in docker := Seq(
  ImageName("gerritforge/gatling-sbt-gerrit-test"),
)
