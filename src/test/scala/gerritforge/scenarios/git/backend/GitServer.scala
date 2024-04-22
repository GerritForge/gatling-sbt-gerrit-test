package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder

trait GitServer {

  def createChange(origin: String, ref: String, userId: String)(
      implicit
      conf: GatlingGitConfiguration
  ): GitRequestBuilder

}
