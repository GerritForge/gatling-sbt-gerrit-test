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
import io.circe.parser.decode
import io.gatling.core.Predef.{bodyString, _}
import io.gatling.http.Predef.http
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.util.Random

object ChangeList {

  val randomNumber = new Random

  def apply(): HttpRequestBuilder = {
    http("changes list and get first change")
      .get("/q/status:open")
      .headers(restApiHeader)
      .resources(
        http("get server version")
          .get("/config/server/version"),
        http("get server info")
          .get("/config/server/info"),
        http("get list of changes")
          .get("/changes/?O=81&S=0&n=500&q=status%3Aopen")
          .check(
            bodyString.transform(_.drop(XSS_LEN))
              .transform(decode[List[ChangeDetail]](_))
              .transform(_.right.get)
              .transform(changes => changes(randomNumber.nextInt(changes.size)))
              .saveAs("changeDetail")))
  }
}
