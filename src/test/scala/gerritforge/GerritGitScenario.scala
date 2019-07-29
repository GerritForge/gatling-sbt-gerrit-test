package gerritforge

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._

object GerritGitScenario {

  implicit val gitConfig = GatlingGitConfiguration()

  val cloneCommand = new GitRequestBuilder(
    GitRequestSession("clone", s"${testConfig.sshUrl}/${testConfig.project}", "${refSpec}")
  )

  val pushCommand = new GitRequestBuilder(
    GitRequestSession("push", s"${testConfig.sshUrl}/${testConfig.project}", "${refSpec}")
  )
}
