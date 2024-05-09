package gerritforge.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object BitBucketConfig {
  val bitBucketConfig = ConfigSource.default.at("bitbucket").loadOrThrow[BitBucketConfig]
}

final case class BitBucketConfig(
    projectKey: String,
    slug: String
)
