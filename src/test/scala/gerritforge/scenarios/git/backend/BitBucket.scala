package gerritforge.scenarios.git.backend
import com.github.barbasa.gatling.git.GatlingGitConfiguration
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.http

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Random

case class BitBucket(username: String, password: String, repository: String) extends GitServer {

  def apiBaseUrl(projKey: String = "LOAD") = s"rest/api/latest/projects/$projKey/repos/$repository"
  val basicAuthValue =
    Base64.getEncoder
      .encodeToString(s"$username:$password".getBytes(StandardCharsets.UTF_8))

  override val refSpecFeeder: Iterator[Map[String, String]] =
    Iterator
      .continually(
        Map("refSpec" -> s"branch-${Random.nextInt(999999)}")
      )

  override def createChange(
      origin: String,
      ref: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ) = {
    new ChainBuilder(
      List(
        http("Create Change over http")
          .post(s"$origin/${apiBaseUrl()}/pull-requests")
          .headers(
            Map(
              "Authorization" -> s"Basic $basicAuthValue",
              "Content-Type"  -> "application/json"
            )
          )
          .body(StringBody(s"""{
                          |    "title": "Change from ref $ref",
                          |    "description": "Test description for from ref $ref",
                          |    "state": "OPEN",
                          |    "open": true,
                          |    "closed": false,
                          |    "fromRef": {
                          |        "id": "refs/heads/$ref",
                          |        "repository": {
                          |            "slug": "load-test",
                          |            "name": null,
                          |            "project": {
                          |                "key": "LOAD"
                          |            }
                          |        }
                          |    },
                          |    "toRef": {
                          |        "id": "refs/heads/master",
                          |        "repository": {
                          |            "slug": "load-test",
                          |            "name": null,
                          |            "project": {
                          |                "key": "LOAD"
                          |            }
                          |        }
                          |    },
                          |    "locked": false,
                          |    "reviewers": []
                          |}""".stripMargin))
      )
    )
  }

  override def baseHttpUrl(url: String): String = url + "/scm/load"

  override val httpUrlSuffix: String = ".git"
}
