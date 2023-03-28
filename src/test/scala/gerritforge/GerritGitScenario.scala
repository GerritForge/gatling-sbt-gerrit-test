package gerritforge

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder

case class GerritGitScenario(gitUrl: String) {

  implicit val gitConfig = GatlingGitConfiguration()

  val cloneCommand: ActionBuilder =
    new GitRequestBuilder(
      GitRequestSession(
        "clone",
        s"$gitUrl/${testConfig.encodedProject}",
        "#{refSpec}",
        ignoreFailureRegexps = List(".*want.+not valid.*")
      )
    )

  val pushCommand: ActionBuilder =
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        s"$gitUrl/${testConfig.encodedProject}",
        "#{refSpec}",
        force = "#{force}",
        ignoreFailureRegexps = List(".*no common ancestry.*")
      )
    )

  val createChangeCommand: ActionBuilder =
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        s"$gitUrl/${testConfig.encodedProject}",
        "HEAD:refs/for/#{refSpec}",
        force = true,
        computeChangeId = true,
        ignoreFailureRegexps = List(".*no common ancestry.*"),
        pushOptions = "t=#{hashtagId}"
      )
    )
}
