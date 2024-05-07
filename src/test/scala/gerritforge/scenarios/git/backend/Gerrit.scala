package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder.toActionBuilder
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object Gerrit extends GitServer {

  override def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder = {
    def pushChange(hashtag: String) = {
      new GitRequestBuilder(
        GitRequestSession(
          "push",
          origin,
          s"HEAD:$refSpec",
          computeChangeId = true,
          pushOptions = s"hashtag=$hashtag,hashtag=#{userId}",
          userId = userId,
          requestName = s"Push new change over $protocol",
          repoDirOverride = s"/tmp/$protocol-#{userId}"
        )
      )
    }

    def createChangePerHashtags = {
      hashtags.map { hashtag =>
        toActionBuilder(
          pushChange(hashtag)
        )
      }.toList
    }

    new ChainBuilder(createChangePerHashtags)
  }
}
