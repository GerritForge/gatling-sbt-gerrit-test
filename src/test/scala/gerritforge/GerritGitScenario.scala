package gerritforge

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._

case class GerritGitScenario(gitUrl: String) {

  implicit val gitConfig = GatlingGitConfiguration()

  val cloneCommand = new GitRequestBuilder(
    GitRequestSession("clone", s"$gitUrl/${testConfig.project}", "${refSpec}")
  )

  val pushCommand = new GitRequestBuilder(
    GitRequestSession("push", s"$gitUrl/${testConfig.project}", "${refSpec}", force = "${force}")
  )

  val createChangeCommand = new GitRequestBuilder(
    GitRequestSession("push", s"$gitUrl/${testConfig.project}", "HEAD:refs/for/${refSpec}", force = true, computeChangeId = true)
  )
}
