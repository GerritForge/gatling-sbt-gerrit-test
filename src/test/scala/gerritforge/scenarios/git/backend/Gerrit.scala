package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._

case class Gerrit(repository: String) extends GitServer {

  override def createChange(origin: String, refSpec: String, userId: String, protocol: String)(
      implicit
      conf: GatlingGitConfiguration
  ) = {
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        s"origin/$repository",
        s"HEAD:$refSpec",
        computeChangeId = true,
        pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
        userId = userId,
        requestName = s"Push to new branch over $protocol"
      )
    )
  }

  override def baseHttpUrl(url: String): String = url + "/a"

  override val refSpecFeeder: IndexedSeq[Map[String, String]] =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> "refs/for/master")
    }
}
