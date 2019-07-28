package gerritforge

import org.scalatest.{FlatSpec, Matchers}
import GerritTestConfig._

class GerritTestConfigSpec extends FlatSpec with Matchers {

  "GerritTestConfig" should "have a cookie" in {
    testConfig.accountCookie should not be empty
  }

  it should "assign the cookie to the Gerrit domain" in {
    val cookieDomain = testConfig.cookie.domain
    cookieDomain should not be empty

    testConfig.url should endWith(cookieDomain.get)
  }
}
