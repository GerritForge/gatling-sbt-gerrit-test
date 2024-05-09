package gerritforge.scenarios.git.backend
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.config.BitBucketConfig.bitBucketConfig._
import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.concurrent.duration.DurationInt
import scala.util.Random

class BitBucket(username: String, password: String, repository: String) extends GitServer {

  def apiBaseUrl = s"rest/api/latest/projects/$projectKey/repos/$repository"
  val basicAuthValue =
    Base64.getEncoder
      .encodeToString(s"$username:$password".getBytes(StandardCharsets.UTF_8))

  override val refSpecFeeder: Iterator[Map[String, String]] =
    Iterator
      .continually(
        Map("refSpec" -> s"branch-${Random.nextInt(99999)}-${System.currentTimeMillis}")
      )

  override def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      pushOptions: Option[String] = Option.empty
  )(
      implicit conf: GatlingGitConfiguration
  ) = {
    exec(
      new GitRequestBuilder(
        GitRequestSession(
          "push",
          origin,
          refSpec,
          userId = "#{userId}",
          requestName = s"Create branch over $protocol",
          repoDirOverride = "/tmp/#{userId}"
        )
      )
    ).pause(500.milliseconds)
      .exec(
        http("Create Pull Request over http")
          .post(s"${simulationConfig.httpUrl.get}/$apiBaseUrl/pull-requests")
          .headers(
            Map(
              "Authorization" -> s"Basic $basicAuthValue",
              "Content-Type"  -> "application/json"
            )
          )
          .body(StringBody(s"""{
                          |    "title": "Pull Request from branch $refSpec",
                          |    "description": "Test description from branch $refSpec - ${System.currentTimeMillis}",
                          |    "state": "OPEN",
                          |    "open": true,
                          |    "closed": false,
                          |    "fromRef": {
                          |        "id": "$refSpec",
                          |        "repository": {
                          |            "slug": "$slug",
                          |            "name": null,
                          |            "project": {
                          |                "key": "$projectKey"
                          |            }
                          |        }
                          |    },
                          |    "toRef": {
                          |        "id": "$MasterRef",
                          |        "repository": {
                          |            "slug": "$slug",
                          |            "name": null,
                          |            "project": {
                          |                "key": "$projectKey"
                          |            }
                          |        }
                          |    },
                          |    "locked": false,
                          |    "reviewers": []
                          |}""".stripMargin))
      )
  }

  override def baseUrl(url: String): String =
    if (url.startsWith("http"))
      url + "/scm/load"
    else url + "/load"

  override val httpUrlSuffix: String = ".git"
}
