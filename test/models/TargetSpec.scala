/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TargetSpec extends AnyWordSpec with Matchers {

  "Target.fromKey" should {

    "return Returns for any returns key" in {
      Target.fromKey("returnDue") mustBe Some(Target.Returns)
      Target.fromKey("returnHistory") mustBe Some(Target.Returns)
      Target.fromKey("amendReturn") mustBe Some(Target.Returns)
      Target.fromKey("manageYourCisReturn") mustBe Some(Target.Returns)
    }

    "return Notices for any notices key" in {
      Target.fromKey("newNotices") mustBe Some(Target.Notices)
      Target.fromKey("noticesAndStatements") mustBe Some(Target.Notices)
    }

    "return Subcontractor for any subcontractor key" in {
      Target.fromKey("subcontractors") mustBe Some(Target.Subcontractor)
    }

    "return ManageContractorDetails for any manage contractor details key" in {
      Target.fromKey("contractorDetails") mustBe Some(Target.ManageContractorDetails)
    }

    "return None for an unknown key" in {
      Target.fromKey("somethingElse") mustBe None
    }
  }
}
