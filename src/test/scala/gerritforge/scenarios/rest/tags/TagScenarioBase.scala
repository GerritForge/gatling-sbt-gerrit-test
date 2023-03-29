package gerritforge.scenarios.rest.tags

import gerritforge.scenarios.rest.RestScenarioBase
import io.gatling.core.Predef._

trait TagScenarioBase extends RestScenarioBase {

  case class TagDetail(ref: String, revision: String)

  val numTagGroups       = 500
  val tagsToDeleteAtOnce = 150

  def tagGroupIds = {
    /*
      We pad groupIds to 3 digits, so that groupId 1 becomes 001, so on so forth.
      This is to avoid that when querying for groupId 1 we also select group 10,11,100,[...]
     */
    def padWithLeadingZeros(num: Int) = f"$num%03d"

    (1 to numTagGroups).map(tagGroup => Map("tagGroupId" -> padWithLeadingZeros(tagGroup))).circular
  }

}
