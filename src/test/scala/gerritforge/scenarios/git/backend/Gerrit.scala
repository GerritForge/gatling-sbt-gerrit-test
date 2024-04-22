package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder.toActionBuilder
import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

case class Gerrit(repository: String) extends GitServer {

  override def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder = {
    new ChainBuilder(hashtags.map { hashtag =>
      toActionBuilder(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            origin,
            s"HEAD:$refSpec",
            computeChangeId = true,
            pushOptions = s"hashtag=$hashtag,hashtag=#{userId}",
            userId = userId,
            requestName = s"Push to new branch over $protocol"
          )
        )
      )
    }.toList)
  }

  override def baseHttpUrl(url: String): String = url + "/a"

  override val refSpecFeeder: IndexedSeq[Map[String, String]] =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> "refs/for/master")
    }
}
