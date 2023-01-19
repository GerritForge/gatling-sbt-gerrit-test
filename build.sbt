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
  "-language:postfixOps",
  "-Ywarn-unused:imports",  // Warn if an import selector is not referenced
  "-Ywarn-unused:locals",   // Warn if a local definition is unused
  "-Ywarn-unused:patvars",  // Warn if a variable bound in a pattern is unused
  "-Ywarn-unused:privates", // Warn if a private member is unused
  "-Xfatal-warnings"        // Fail if there are any compile time warnings
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
  "io.gatling"            % "gatling-app"               % gatlingVer, // Need to avoid "Couldn't locate Gatling
  // libraries in the classpath" when creating docker image
  "com.gerritforge"       %% "gatling-git"              % "2.0.1" excludeAll (
    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers ++= Resolver.sonatypeOssRepos("snapshots") ++ Seq(
  "Eclipse JGit Snapshots" at "https://repo.eclipse.org/content/groups/jgit"
)

name := "gatling-sbt-gerrit-test"
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
  val sshStageFiles = Seq("id_rsa", "config").map { fileName =>
    new File(fileName) -> s"/staging/.ssh/$fileName"
  }
  val gitStageConfigFile = Seq(".gitconfig").map { fileName =>
    new File(fileName) -> s"/staging/$fileName"
  }

  classpath.files.map { f =>
    f -> s"/lib/${f.getName}"
  } ++ simulationFiles ++ confFiles ++ sshStageFiles ++ gitStageConfigFile
}

dockerCommands ++= {
  import com.typesafe.sbt.packager.docker._
  val sshFiles = Seq("id_rsa", "config").map { fileName =>
    Cmd("Add", s"/opt/gatling/staging/.ssh/$fileName", s"/root/.ssh/$fileName")
  }
  val gitConfig = Seq(".gitconfig").map { fileName =>
    Cmd("Add", s"/opt/gatling/staging/$fileName", s"/root/$fileName")
  }
  sshFiles ++ gitConfig
}

scalafmtOnCompile := true
