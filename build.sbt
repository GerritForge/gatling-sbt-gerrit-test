enablePlugins(GatlingPlugin, DockerPlugin, GitVersioning)

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
  "com.gerritforge"       %% "gatling-git"              % "3.1.0" excludeAll (
    ExclusionRule(organization = "io.gatling"),
    ExclusionRule(organization = "io.gatling.highcharts")
  )
)

resolvers ++= Resolver.sonatypeOssRepos("snapshots") ++ Seq(
  "Eclipse JGit Snapshots" at "https://repo.eclipse.org/content/groups/jgit"
)

docker / dockerfile := {
  val classpath = (Compile / managedClasspath).value
  new Dockerfile {
    from(s"gerritforge/gatling:$gatlingVer")

    stageFiles(
      Seq("src/test/resources/logback.xml", "src/test/resources/application.conf").map(new File(_)),
      "conf/"
    )
    stageFile(new File("src/test/scala/gerritforge"), "simulations/")
    copyRaw("simulations/", "/opt/gatling/user-files/simulations/")
    copyRaw("conf/", "/opt/gatling/conf/")
    stageFiles(Seq("id_rsa", "id_rsa.pub", "config").map(new File(_)), ".ssh/")
    addRaw(".ssh/", "/root/.ssh/")
    stageFile(new File(".gitconfig"), ".gitconfig")
    addRaw(".gitconfig", ".gitconfig")
    addRaw(".gitconfig", "/root/")

    add(classpath.files, "/opt/gatling/lib/")
  }
}

docker / imageNames := Seq(
  ImageName("gerritforge/gatling-sbt-gerrit-test:latest"),
  ImageName(s"gerritforge/gatling-sbt-gerrit-test:${version.value}")
)

scalafmtOnCompile := true
