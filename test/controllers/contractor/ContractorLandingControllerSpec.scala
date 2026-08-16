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

package controllers.contractor

import base.SpecBase
import config.FrontendAppConfig
import controllers.contractor.ContractorLandingController.fromUserAnswers
import models.{Scheme, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.CisIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Call}
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import views.html.contractor.ContractorLandingView

import scala.concurrent.Future

class ContractorLandingControllerSpec extends SpecBase {

  lazy val contractorLandingRoute: String =
    controllers.contractor.routes.ContractorLandingController.onPageLoad().url

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, contractorLandingRoute)

  "ContractorLandingController" - {

    "must return OK and the correct view for a GET" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.successful(("CIS-123", emptyUserAnswers)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        implicit val appConfig: FrontendAppConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request  = FakeRequest(GET, routes.ContractorLandingController.onPageLoad().url)
        val result   = route(application, request).value
        val view     = application.injector.instanceOf[ContractorLandingView]
        val vm       = fromUserAnswers(emptyUserAnswers, appConfig)
        val expected = view(vm)(request, messages(application))

        status(result)          shouldBe OK
        contentType(result)       should contain(HTML)
        contentAsString(result) shouldBe expected.toString
      }
    }

    "must redirect to Journey Recovery when service return NOT_FOUND" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", NOT_FOUND, NOT_FOUND)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to system error page if unable to retrieve cisId" in {
      val mockManageService = mock[ManageService]
      when(mockManageService.resolveAndStoreCisId(any[UserAnswers])(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ManageService].toInstance(mockManageService)
        )
        .build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }

  "ContractorLandingController.onTargetClick" - {
    val returnsTargetKey         = "returnDue"
    val instanceId               = "CIS-123"
    val uaWithCisId: UserAnswers =
      emptyUserAnswers
        .set(CisIdPage, instanceId)
        .success
        .value

    "must call prepopulate + getScheme and redirect when cisId and employerReference are present and scheme exists" in {
      val mockPrepopService = mock[PrepopService]

      val scheme = Scheme(
        schemeId = 123,
        instanceId = instanceId,
        utr = None,
        name = None,
        prePopSuccessful = None,
        subcontractorCounter = None
      )

      when(
        mockPrepopService.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(Some(scheme)))

      // controller wraps this in Redirect(...)
      when(
        mockPrepopService.determineLandingDestination(
          any[Call],
          any[String],
          any[Scheme],
          any[Call],
          any[Call]
        )
      ).thenReturn(controllers.routes.ReturnsLandingController.onPageLoad(instanceId))

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController.onTargetClick(returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must endWith(
          controllers.routes.ReturnsLandingController.onPageLoad(instanceId).url
        )

        verify(mockPrepopService)
          .prepopulateContractorKnownFacts(eqTo(instanceId), any[String], any[String])(any[HeaderCarrier])
        verify(mockPrepopService)
          .getScheme(eqTo(instanceId))(any[HeaderCarrier])
      }
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with UpstreamErrorResponse" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(UpstreamErrorResponse("boom", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
      )

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController
              .onTargetClick(returnsTargetKey)
              .url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemErrorController when prepopulateContractorKnownFacts fails with an unexpected error" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(
        Future.failed(new RuntimeException("boom"))
      )

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController
              .onTargetClick(returnsTargetKey)
              .url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemErrorController when CisIdPage is missing" in {
      val mockPrepopService = mock[PrepopService]

      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController
              .onTargetClick(returnsTargetKey)
              .url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad().url

        verifyNoInteractions(mockPrepopService)
      }
    }

    "must return NotFound when targetKey is unknown" in {
      val mockPrepopService = mock[PrepopService]

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController
              .onTargetClick("unknownTargetKey")
              .url
          )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
        verifyNoInteractions(mockPrepopService)
      }
    }

    "must redirect to SystemErrorController when getScheme returns None" in {
      val mockPrepopService = mock[PrepopService]

      when(
        mockPrepopService.prepopulateContractorKnownFacts(any[String], any[String], any[String])(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(None))

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        ).overrides(
          bind[PrepopService].toInstance(mockPrepopService)
        ).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController.onTargetClick(returnsTargetKey).url
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must pass contractor details management call to determineLandingDestination when contractorDetails target is selected" in {

      val mockPrepopService = mock[PrepopService]

      val scheme = Scheme(
        schemeId = 123,
        instanceId = instanceId,
        utr = Some("1234567890"),
        name = Some("ABC Ltd"),
        prePopSuccessful = Some("Y"),
        subcontractorCounter = Some(5)
      )

      when(
        mockPrepopService.prepopulateContractorKnownFacts(
          any[String],
          any[String],
          any[String]
        )(any[HeaderCarrier])
      ).thenReturn(Future.unit)

      when(
        mockPrepopService.getScheme(any[String])(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(Some(scheme))
      )

      when(
        mockPrepopService.determineLandingDestination(
          any[Call],
          any[String],
          any[Scheme],
          any[Call],
          any[Call]
        )
      ).thenReturn(
        controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(uaWithCisId),
          additionalBindings = Seq.empty
        )
          .overrides(
            bind[PrepopService].toInstance(mockPrepopService)
          )
          .build()

      running(application) {

        val appConfig =
          application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(
            GET,
            controllers.contractor.routes.ContractorLandingController
              .onTargetClick("contractorDetails")
              .url
          )

        val result =
          route(application, request).value

        status(result) mustBe SEE_OTHER

        val targetCallCaptor =
          ArgumentCaptor.forClass(classOf[Call])

        verify(mockPrepopService).determineLandingDestination(
          targetCallCaptor.capture(),
          eqTo(instanceId),
          eqTo(scheme),
          any[Call],
          any[Call]
        )

        val capturedTargetCall =
          targetCallCaptor.getValue

        capturedTargetCall.method mustBe GET
        capturedTargetCall.url mustBe appConfig.contractorDetailsManagementUrl
      }
    }
  }
}
