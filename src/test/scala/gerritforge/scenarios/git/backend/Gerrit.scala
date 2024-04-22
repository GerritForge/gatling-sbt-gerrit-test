package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.action.GitRequestActionBuilder
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder.toActionBuilder
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

final case class Gerrit(repository: String, numUsers: Int) extends GitServer {

  override def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      pushOptions: Option[String] = Option.empty
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder = {
    def pushChange: GitRequestActionBuilder = {
      toActionBuilder(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            origin,
            s"HEAD:$refSpec",
            computeChangeId = true,
            pushOptions = pushOptions.fold("")(identity),
            userId = userId,
            requestName = s"Push to create change over $protocol",
            repoDirOverride = s"/tmp/$protocol-#{userId}"
          )
        )
      )
    }

    new ChainBuilder(List(pushChange))
  }

  def createChangePerHashtag(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder = {
    new ChainBuilder(
      hashtags
        .map(
          hashtag =>
            createChange(
              origin,
              refSpec,
              userId,
              protocol,
              Option(s"hashtag=$hashtag,hashtag=#{userId}")
            )
        )
        .flatMap(_.actionBuilders)
        .toList
    )
  }

  override def baseUrl(url: String): String =
    if (url.startsWith("http"))
      url + "/a"
    else url


  override val refSpecFeeder: Iterator[Map[String, String]] =
    Iterator
      .continually(
        Map("refSpec" -> "refs/for/master")
      )
}
