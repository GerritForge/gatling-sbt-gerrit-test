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

val gatlingVer = "3.8.4"

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
    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers ++= Resolver.sonatypeOssRepos("snapshots") ++ Seq(
  "Eclipse JGit Snapshots" at "https://repo.eclipse.org/content/groups/jgit"
)

name := "gerritforge/gatling-sbt-gerrit-test"
dockerUpdateLatest := true
dockerBaseImage := s"gerritforge/gatling:$gatlingVer"
Docker / daemonUserUid := None
Docker / daemonUser := "daemon"
dockerBuildxPlatforms := Seq("linux/arm64/v8", "linux/amd64")
Docker / defaultLinuxInstallLocation := "/opt/gatling"
Universal / mappings ++= {
  val classpath = (Compile / managedClasspath).value
  val confFiles = Seq(
    new File("src/test/resources/application.conf") -> "/conf/application.conf",
    new File("src/test/resources/logback.xml")      -> "/conf/logback.xml"
  )
  val simulationFiles = new File("src/test/scala/gerritforge")
    .listFiles()
    .map(f => f -> s"user-files/simulations/${f.getName}")
  val sshFiles = Seq("id_rsa", "id_rsa.pub", "config").map { fileName =>
    new File(fileName) -> s"/root/.ssh/$fileName" //TODO: These are being written in `/opt/gatling/root/.ssh`, rather than `/root/.ssh`
  }
  val gitConfig = Seq(".gitconfig").map { fileName =>
    new File(fileName) -> s"/root/$fileName" //TODO: same todo as above
  }

  classpath.files.map { f =>
    f -> s"/lib/${f.getName}"
  } ++ simulationFiles ++ confFiles ++ sshFiles ++ gitConfig
}

scalafmtOnCompile := true
