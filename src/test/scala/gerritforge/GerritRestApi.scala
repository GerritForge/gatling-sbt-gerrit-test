package gerritforge

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef.{BlackList, WhiteList}
import io.gatling.http.HttpDsl

trait GerritRestApi extends HttpDsl {

  val XSS_LEN = 5

  val restApiHeader = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma" -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  def postApiHeader(xsrfCookie: Option[String]) = {
    val headers: Map[String,String] = restApiHeader + ("Content-Type" -> "application/json")
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }
}
