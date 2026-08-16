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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers.*
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.contractor.ContractorLandingViewModel
import views.html.contractor.ContractorLandingView

class ContractorLandingViewSpec extends SpecBase {

  private val schemeName        = "ABC Construction Ltd"
  private val employerReference = "123/AB45678"

  private val viewModel = ContractorLandingViewModel(
    schemeName = schemeName,
    employerReference = employerReference,
    whatIsUrl = "https://www.gov.uk/what-is-the-construction-industry-scheme",
    guidanceUrl = "https://www.gov.uk/guidance/cis-monthly-returns",
    penaltiesUrl = "https://www.gov.uk/government/publications/cis-340"
  )

  "ContractorLandingView" - {

    "render the page with expected header and title" in {
      val doc = render()

      doc.title() shouldBe s"${messages(app)("contractorLanding.title")} - ${messages(app)("service.name")} - GOV.UK"

      val h1 = doc.selectFirst("h1")
      h1.text() shouldBe messages(app)("contractorLanding.heading")
    }

    "show employerReference and schemeName in a summary list" in {
      val doc = render()

      val rows = doc.select(".govuk-summary-list .govuk-summary-list__row")
      rows.size() shouldBe 2

      val schemeNameKey   = rows.get(0).selectFirst(".govuk-summary-list__key").text()
      val schemeNameValue = rows.get(0).selectFirst(".govuk-summary-list__value").text()
      schemeNameKey   shouldBe messages(app)("contractorLanding.label.schemeName")
      schemeNameValue shouldBe schemeName

      val employerKey   = rows.get(1).selectFirst(".govuk-summary-list__key").text()
      val employerValue = rows.get(1).selectFirst(".govuk-summary-list__value").text()
      employerKey   shouldBe messages(app)("contractorLanding.label.employerReference")
      employerValue shouldBe employerReference
    }

    "render titles and paragraphs for each card" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourCisReturn.title"))
      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourCisReturn.p"))

      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourSubcontractors.title"))
      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourSubcontractors.p"))

      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourContractorDetails.title"))
      doc.text() should include(messages(app)("contractorLanding.landing.card.manageYourContractorDetails.p"))

      doc.text() should include(messages(app)("contractorLanding.landing.card.appealPenalty.title"))
      doc.text() should include(messages(app)("contractorLanding.landing.card.appealPenalty.p"))

      doc.text() should include(messages(app)("contractorLanding.landing.card.noticesAndStatements.title"))
      doc.text() should include(messages(app)("contractorLanding.landing.card.noticesAndStatements.p"))
    }

    "render sidebar with help and guidance" in {
      val doc = render()

      doc.text() should include(messages(app)("contractorLanding.sidebar.title"))

      doc.text() should include(messages(app)("contractorLanding.sidebar.nav.whatIs.text"))
      doc.text() should include(messages(app)("contractorLanding.sidebar.nav.guidance.text"))
      doc.text() should include(messages(app)("contractorLanding.sidebar.nav.penalties.text"))

      val links     = doc.select("a[href]")
      val linkTexts = links.eachText()

      linkTexts should contain(messages(app)("contractorLanding.sidebar.nav.whatIs.text"))
      linkTexts should contain(messages(app)("contractorLanding.sidebar.nav.guidance.text"))
      linkTexts should contain(messages(app)("contractorLanding.sidebar.nav.penalties.text"))

      links.select("[href=https://www.gov.uk/what-is-the-construction-industry-scheme]").size() shouldBe 1
      links.select("[href=https://www.gov.uk/guidance/cis-monthly-returns]").size()             shouldBe 1
      links.select("[href=https://www.gov.uk/government/publications/cis-340]").size()          shouldBe 1
    }

    "render all landing page cards with correct links" in {
      val doc = render()

      doc
        .getElementById("manage-your-cis-return")
        .selectFirst("a")
        .attr("href") shouldBe
        controllers.contractor.routes.ContractorLandingController
          .onTargetClick("manageYourCisReturn")
          .url

      doc
        .getElementById("manage-your-subcontractors")
        .selectFirst("a")
        .attr("href") shouldBe
        controllers.contractor.routes.ContractorLandingController
          .onTargetClick("subcontractors")
          .url

      doc
        .getElementById("manage-your-contractor-details")
        .selectFirst("a")
        .attr("href") shouldBe
        controllers.contractor.routes.ContractorLandingController
          .onTargetClick("contractorDetails")
          .url
    }
  }

  private def render(): Document = {
    val application = app

    implicit val request: Request[AnyContent] = FakeRequest(GET, "/contractor-dashboard")
    implicit val msgs: Messages               = messages(application)

    val view = application.injector.instanceOf[ContractorLandingView]
    val html = view(viewModel)

    Jsoup.parse(html.body)
  }
}
