package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.backend.{Gerrit, GitServer}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CreateChangeCommand(val gitServer: GitServer, val url: String, scenarioHashtags: Seq[String])
    extends GitScenarioBase {

  val hashtagLoop = scenarioHashtags.to(LazyList).lazyAppendedAll(scenarioHashtags)

  override def scn: ScenarioBuilder =
    scenario(s"Create Change Command over $protocol")
      .feed(gitServer.refSpecFeeder)
      .feed(userIdFeeder.circular)
      .doIf { session =>
        !alreadyFedUsers.contains(session("userId").as[String])
      } {
        exec { session =>
          alreadyFedUsers = session("userId").as[String] :: alreadyFedUsers
          session
        }.exec(
          new GitRequestBuilder(
            // We only do a "git pull" once to setup the client environment.
            // All the changes created will be chained.
            GitRequestSession(
              "pull",
              simulationConfig.referenceRepository
                .fold(gitServer.gitUrl(url, simulationConfig.repository))(identity),
              MasterRef,
              userId = "#{userId}",
              requestName = s"Pull to setup create change over $protocol",
              ignoreFailureRegexps = List(".*want.+not valid.*"),
              repoDirOverride = s"/tmp/$protocol-#{userId}"
            )
          )
        )
      }
      .exec {
        gitServer match {
          case server: Gerrit.type =>
            server.createChangePerHashtag(
              gitServer.gitUrl(url, simulationConfig.repository),
              "#{refSpec}",
              "#{userId}",
              protocol,
              scenarioHashtags
            )
          case _ =>
            gitServer
              .createChange(
                gitServer.gitUrl(url, simulationConfig.repository),
                "#{refSpec}",
                "#{userId}",
                protocol
              )
        }
      }
      .pause(pauseDuration, pauseStdDev)
}
