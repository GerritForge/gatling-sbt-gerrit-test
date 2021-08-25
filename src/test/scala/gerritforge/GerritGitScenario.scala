package gerritforge

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder

case class GerritGitScenario(gitUrl: Option[String]) {

  implicit val gitConfig = GatlingGitConfiguration()

  val cloneCommand: Option[ActionBuilder] = gitUrl.map(
    url =>
      new GitRequestBuilder(
        GitRequestSession(
          "clone",
          s"$url/${testConfig.project}",
          "${refSpec}",
          ignoreFailureRegexps = List(".*want.+not valid.*")
        )
      )
  )

  val pushCommand: Option[ActionBuilder] = gitUrl.map(
    url =>
      new GitRequestBuilder(
        GitRequestSession(
          "push",
          s"$url/${testConfig.project}",
          "${refSpec}",
          force = "${force}",
          ignoreFailureRegexps = List(".*no common ancestry.*")
        )
      )
  )

  val createChangeCommand: Option[ActionBuilder] = gitUrl.map(
    url =>
      new GitRequestBuilder(
        GitRequestSession(
          "push",
          s"$url/${testConfig.project}",
          "HEAD:refs/for/${refSpec}",
          force = true,
          computeChangeId = true,
          ignoreFailureRegexps = List(".*no common ancestry.*")
        )
      )
  )
}
