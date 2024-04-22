package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import io.gatling.core.Predef._

object Gerrit extends GitServer {

  override def createChange(origin: String, ref: String, userId: String)(
      implicit
      conf: GatlingGitConfiguration
  ): GitRequestBuilder = {
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        origin,
        s"HEAD:refs/for/$ref",
        computeChangeId = true,
        pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
        userId = userId,
        requestName = "Push to new branch"
      )
    )
  }
}
