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

import java.net.HttpURLConnection.{HTTP_FORBIDDEN, HTTP_NO_CONTENT, HTTP_OK}

import gerritforge.requests.Headers._
import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, status, _}
import io.gatling.http.request.builder.HttpRequestBuilder

object ChangeDetails {

  def apply(authCookie: Option[String] = None, xsrfCookie: Option[String] = None): HttpRequestBuilder = {
    val checkStatus = status.in(authCookie.fold(Seq(HTTP_FORBIDDEN))(_ => Seq(HTTP_OK, HTTP_NO_CONTENT)))

    val CHANGE = "/changes/"
    http("get change details")
      .get("${changeUrl}")
      .headers(restApiHeader)
      .resources(
        http("check account capabilities")
          .get("/accounts/self/capabilities")
          .check(checkStatus),
        http("fetch comments")
          .get(CHANGE + "${id}/comments"),
        http("fetch robot-comments")
          .get(CHANGE + "${id}/robotcomments"),
        http("get change detail")
          .get(CHANGE + "${id}/detail?O=916314"),
        http("get draft comments")
          .get(CHANGE + "${id}/drafts")
          .check(checkStatus),
        http("get download commands")
          .get(CHANGE + "${id}/edit/?download-commands=true")
          .check(checkStatus),
        http("get project config")
          .get("/projects/${project}/config"),
        http("get available actions")
          .get(CHANGE + "${id}/revisions/1/actions"),
        http("get list of reviewed files")
          .get(CHANGE + "${id}/revisions/1/files?reviewed")
          .check(checkStatus),
        http("check if change is mergeable")
          .get(CHANGE + "${id}/revisions/current/mergeable"),
        http("get related changes")
          .get(CHANGE + "${id}/revisions/1/related"),
        http("get cherry picks")
          .get(CHANGE + "?O=a&q=project%3A${project}%20change%3A${changeId}%20-change%3A${changeNum}%20-is%3Aabandoned"),
        http("get conflicting changes")
          .get(CHANGE + "?O=a&q=status%3Aopen%20conflicts%3A${changeNum}"),
        http("check for other changes submittable together")
          .get(CHANGE + "${id}/submitted_together?o=NON_VISIBLE_CHANGES"))
  }

}
