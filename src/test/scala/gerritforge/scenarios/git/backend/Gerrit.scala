package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import io.gatling.core.Predef._

object Gerrit extends GitServer {

  override def createChange(origin: String, refSpec: String, userId: String, protocol: String)(
      implicit
      conf: GatlingGitConfiguration
  ): GitRequestBuilder = {
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        origin,
        s"HEAD:$refSpec",
        computeChangeId = true,
        pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
        userId = userId,
        requestName = s"Push new change over $protocol",
        repoDirOverride = s"/tmp/$protocol-#{userId}"
      )
    )
  }
}
