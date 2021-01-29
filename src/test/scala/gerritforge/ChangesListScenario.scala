package gerritforge

import gerritforge.chains.AuthCookieBuilder
import gerritforge.ops.ChainBuilderOps._
import gerritforge.requests.{ChangeDetails, ChangeList, PostComment}
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

object ChangesListScenario {

  def listAndCommentChanges(authCookie: Option[String] = None, xsrfCookie: Option[String] = None): ChainBuilder = {

    val listChanges = ChangeList()
    val getChangeDetails = ChangeDetails(authCookie, xsrfCookie)
    val postComments = PostComment(xsrfCookie)
    val httpHead = http("head").head("/")

    AuthCookieBuilder(authCookie)
      .exec(listChanges)
      .asChangeDetails
      .pause(2 seconds)
      .exec(getChangeDetails)
      .exec(authCookie.fold(httpHead)(_ => postComments))
  }
}
