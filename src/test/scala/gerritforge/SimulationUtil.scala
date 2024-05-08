package gerritforge

import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef.{AllowList, DenyList}
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocol
import io.gatling.core.Predef._

object SimulationUtil {

  val httpProtocol: HttpProtocol = simulationConfig.httpUrl
    .map(
      url =>
        http
          .baseUrl(url)
          .inferHtmlResources(
            AllowList(),
            DenyList(""".*\.js""", """.*\.css""", """.*\.ico""", """.*\.woff2""", """.*\.png""")
          )
          .acceptHeader("*/*")
          .acceptEncodingHeader("gzip, deflate")
          .acceptLanguageHeader("en-GB,en;q=0.5")
          .userAgentHeader("gatling-test")
    )
    .getOrElse(
      throw new IllegalArgumentException(
        "GERRIT_HTTP_URL must be defined to run REST-API simulation"
      )
    )

}
