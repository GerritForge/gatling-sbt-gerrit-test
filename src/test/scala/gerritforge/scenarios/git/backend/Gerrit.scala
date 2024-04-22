package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol

case class Gerrit(repository: String) extends GitServer {

  override def createChange(origin: String, ref: String, userId: String, protocol: String)(
      implicit
      conf: GatlingGitConfiguration
  ) = {
    new GitRequestBuilder(
      GitRequestSession(
        "push",
        s"origin/$repository",
        s"HEAD:refs/for/$ref",
        computeChangeId = true,
        pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
        userId = userId,
        requestName = s"Push to new branch over $protocol"
      )
    )
  }

  override def baseHttpUrl(url: String): String = url + "/a"
}
