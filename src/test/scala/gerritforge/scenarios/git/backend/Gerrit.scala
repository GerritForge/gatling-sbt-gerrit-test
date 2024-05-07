package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder.toActionBuilder
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object Gerrit extends GitServer {

  override def createChange(
      origin: String,
      ref: String,
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
            s"HEAD:refs/for/$ref",
            computeChangeId = true,
            pushOptions = s"hashtag=$hashtag,hashtag=#{userId}",
            userId = userId,
            requestName = s"Push to new branch over $protocol"
          )
        )
      )
    }.toList)
  }
}
