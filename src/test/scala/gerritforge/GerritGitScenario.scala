package gerritforge

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import io.gatling.core.Predef._
import GerritTestConfig._

object GerritGitScenario {

  implicit val gitConfig = GatlingGitConfiguration()

  val cloneCommand = new GitRequestBuilder(GitRequestSession(
    "clone",
    s"${testConfig.sshUrl}/${testConfig.project}"))

  val pushCommand = new GitRequestBuilder(GitRequestSession("push", s"${testConfig.sshUrl}/${testConfig.project}"))
}
