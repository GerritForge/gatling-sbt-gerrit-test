// Copyright (C) 2018 GerritForge Ltd
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package gerritforge.requests

import gerritforge.requests.Headers._
import io.gatling.core.Predef.{StringBody, _}
import io.gatling.http.Predef.http
import io.gatling.http.request.builder.HttpRequestBuilder

object PostComment {

  def postApiHeader(xsrfCookie: Option[String]) = {
    val headers: Map[String, String] = restApiHeader + ("Content-Type" -> "application/json")
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }

  def apply(xsrfCookie: Option[String] = None): HttpRequestBuilder = {
    http("Post comments with score")
      .post("/changes/${project}~${changeNum}/revisions/1/review")
      .headers(postApiHeader(xsrfCookie))
      .body(StringBody("""{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":${reviewScore}},"message":"${reviewMessage}","reviewers":[]}"""))
      .asJson
  }
}
