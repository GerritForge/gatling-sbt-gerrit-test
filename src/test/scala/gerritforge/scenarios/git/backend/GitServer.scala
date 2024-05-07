package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import io.gatling.core.structure.ChainBuilder

trait GitServer {

  def createChange(
      origin: String,
      ref: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder

}
