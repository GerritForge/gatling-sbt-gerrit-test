enablePlugins(GatlingPlugin, JavaAppPackaging, DockerPlugin, GitVersioning)
scalaVersion := "2.13.10"
git.useGitDescribe := true
git.formattedShaVersion := git.gitHeadCommit.value map { sha =>
  "v" + sha.take(7)
}

scalacOptions := Seq(
  "-encoding",
  "UTF-8",
  "-release:8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:postfixOps"
)

val gatlingVer = "3.9.0"

val circeVer = "0.13.0"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig"               % "0.13.0",
  "io.circe"              %% "circe-core"               % circeVer,
  "io.circe"              %% "circe-parser"             % circeVer,
  "io.circe"              %% "circe-generic"            % circeVer,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVer % Test,
  "io.gatling"            % "gatling-test-framework"    % gatlingVer % Test,
  "org.scalatest"         %% "scalatest"                % "3.0.8" % Test,
  "io.gatling"            % "gatling-app"               % gatlingVer,
  "com.gerritforge"       %% "gatling-git"              % "2.0.1" excludeAll (
//    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers ++= Resolver.sonatypeOssRepos("snapshots") ++ Seq(
  "Eclipse JGit Snapshots" at "https://repo.eclipse.org/content/groups/jgit"
)

//docker / dockerfile := {
//  val classpath = (Compile / managedClasspath).value
//  new Dockerfile {
//    from(s"gerritforge/gatling:$gatlingVer")
//
//    stageFiles(Seq("src/test/resources/logback.xml", "src/test/resources/application.conf").map(new File(_)), "conf/")
//    stageFile(new File("src/test/scala/gerritforge"), "simulations/")
//    copyRaw("simulations/","/opt/gatling/user-files/simulations/")
//    copyRaw("conf/", "/opt/gatling/conf/")
//    stageFiles(Seq("id_rsa", "id_rsa.pub", "config").map(new File(_)), ".ssh/")
//    addRaw(".ssh/","/root/.ssh/")
//    stageFile(new File(".gitconfig"), ".gitconfig")
//    addRaw(".gitconfig", ".gitconfig")
//    addRaw(".gitconfig", "/root/")
//    classpath.files.foreach(f => println(f.absolutePath))
//    add(classpath.files, "/opt/gatling/lib/")
//  }
//}
//docker / imageNames := Seq(
//  ImageName("gerritforge/gatling-sbt-gerrit-test:latest"),
//  ImageName(s"gerritforge/gatling-sbt-gerrit-test:${version.value}")
//)

name := "gerritforge/gatling-sbt-gerrit-test"
dockerUpdateLatest := true
dockerBaseImage := s"gerritforge/gatling:$gatlingVer"
Docker / daemonUserUid := None
Docker / daemonUser := "daemon"
dockerBuildxPlatforms := Seq("linux/arm64/v8", "linux/amd64")
Docker / defaultLinuxInstallLocation := "/opt/gatling"
Universal / mappings ++= {
  // generates the test package
  val classpath = (Compile / managedClasspath).value
  val confFiles = Seq(
    new File("src/test/resources/application.conf") -> "/conf/application.conf",
    new File("src/test/resources/logback.xml")      -> "/conf/logback.xml"
  )
  val simulationFiles = new File("src/test/scala/gerritforge")
    .listFiles()
    .map(f => f -> s"user-files/simulations/${f.getName}")
  classpath.files.map { f =>
    f -> s"/lib/${f.getName}"
  } ++ simulationFiles ++ confFiles
}

scalafmtOnCompile := true
