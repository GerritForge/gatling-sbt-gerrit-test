package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import io.gatling.core.action.builder.ActionBuilder

trait GitServer {

  def createChange(origin: String, ref: String, userId: String)(
      implicit
      conf: GatlingGitConfiguration
  ): ActionBuilder

}
