package gerritforge.scenarios.git.backend
import com.github.barbasa.gatling.git.GatlingGitConfiguration
import io.gatling.core.Predef.{StringBody, _}
import io.gatling.http.Predef.http

class BitBucket(username: String, password: String, repository: String) extends GitServer {

  override def createChange(origin: String, ref: String, userId: String)(
      implicit conf: GatlingGitConfiguration
  ) = {
    http("Create Change")
      .post(s"2.0/repositories/$username/$repository/pullrequests")
      .headers(Map("u" -> s"$username:$password", "Content-Type" -> "Application-Json"))
      .body(StringBody(s"""{"title":"some-title",
           |"source": {
           |  "branch": {
           |    "name": "some-branch"
           |  }
           |}"""))
  }
}
