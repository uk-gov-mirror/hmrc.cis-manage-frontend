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

import ContractorLandingController.fromUserAnswers
import models.{EmployerReference, Target, UserAnswers}
import pages.*
import config.FrontendAppConfig
import controllers.actions.*
import models.Target.*
import models.requests.DataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.{ManageService, PrepopService}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.http.{HeaderCarrier, HttpVerbs, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.contractor.ContractorLandingViewModel
import views.html.contractor.ContractorLandingView

import javax.inject.{Inject, Named}
import scala.util.control.NonFatal
import scala.concurrent.{ExecutionContext, Future}

class ContractorLandingController @Inject() (
  override val messagesApi: MessagesApi,
  @Named("ContractorIdentifier") identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ContractorLandingView,
  appConfig: FrontendAppConfig,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  manageService: ManageService,
  prepopService: PrepopService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    manageService
      .resolveAndStoreCisId(request.userAnswers)
      .map { case (_, updatedUa) =>
        val viewModel = fromUserAnswers(updatedUa, appConfig)
        Ok(view(viewModel))
      }
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case exception                                             =>
          logger.error(
            s"[ContractorLandingController] Failed to retrieve cisId: ${exception.getMessage}",
            exception
          )
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }

  }

  def onTargetClick(targetKey: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val systemErrorRedirect     = Redirect(controllers.routes.SystemErrorController.onPageLoad())
      val unauthorizedOrgRedirect = Redirect(controllers.routes.UnauthorisedOrganisationAffinityController.onPageLoad())

      resolveInputs(targetKey, systemErrorRedirect, unauthorizedOrgRedirect) match {
        case Left(result) =>
          Future.successful(result)

        case Right((target, instanceId, employerRef)) =>
          handleTargetClick(targetKey, target, instanceId, employerRef, systemErrorRedirect)
      }
    }

  private def resolveInputs(
    targetKey: String,
    systemErrorRedirect: Result,
    unauthorizedOrgRedirect: Result
  )(implicit request: DataRequest[_]): Either[Result, (Target, String, EmployerReference)] =
    Target.fromKey(targetKey) match {
      case None =>
        Left(NotFound("Unknown target"))

      case Some(target) =>
        request.userAnswers.get(CisIdPage) match {
          case None =>
            logger.warn("[ContractorLandingController][onTargetClick] Missing CisIdPage (instanceId) in userAnswers")
            Left(unauthorizedOrgRedirect)

          case Some(instanceId) =>
            request.employerReference match {
              case None =>
                logger.warn("[ContractorLandingController][onTargetClick] Missing employerReference on DataRequest")
                Left(systemErrorRedirect)

              case Some(employerRef) =>
                Right((target, instanceId, employerRef))
            }
        }
    }

  private def handleTargetClick(
    targetKey: String,
    target: Target,
    instanceId: String,
    employerRef: EmployerReference,
    systemErrorRedirect: Result
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val addContractorDetailsCall =
      controllers.routes.AddContractorDetailsController.onPageLoad()

    val checkSubcontractorRecordsCall =
      controllers.routes.CheckSubcontractorRecordsController.onPageLoad(
        employerRef.taxOfficeNumber,
        employerRef.taxOfficeReference,
        instanceId,
        targetKey
      )

    prepopService
      .prepopulateContractorKnownFacts(
        instanceId = instanceId,
        taxOfficeNumber = employerRef.taxOfficeNumber,
        taxOfficeReference = employerRef.taxOfficeReference
      )
      .flatMap(_ => prepopService.getScheme(instanceId))
      .map {
        case Some(scheme) =>
          Redirect(
            prepopService.determineLandingDestination(
              targetCall = targetCall(target, instanceId),
              instanceId = instanceId,
              scheme = scheme,
              addContractorDetailsCall = addContractorDetailsCall,
              checkSubcontractorRecordsCall = checkSubcontractorRecordsCall
            )
          )

        case None =>
          logger.warn(s"[ContractorLandingController][onTargetClick] No scheme found for instanceId=$instanceId")
          systemErrorRedirect
      }
      .recover {
        case u: UpstreamErrorResponse =>
          logger.error(s"[ContractorLandingController][onTargetClick] upstream error: ${u.message}", u)
          systemErrorRedirect

        case NonFatal(e) =>
          logger.error("[ContractorLandingController][onTargetClick] unexpected error", e)
          systemErrorRedirect
      }
  }

  private def targetCall(target: Target, instanceId: String): Call =
    target match {
      case Returns                 => controllers.routes.ReturnsLandingController.onPageLoad(instanceId)
      case Notices                 => controllers.notices.routes.ManageNoticesStatementsController.onPageLoad(instanceId)
      case Subcontractor           => controllers.routes.SubcontractorsLandingPageController.onPageLoad(instanceId)
      case ManageContractorDetails => Call(GET, appConfig.contractorDetailsManagementUrl)
    }
}

object ContractorLandingController {

  def fromUserAnswers(ua: UserAnswers, appConfig: FrontendAppConfig): ContractorLandingViewModel =
    ContractorLandingViewModel(
      schemeName = ua.get(ContractorNamePage).getOrElse(""),
      employerReference = ua.get(EmployerReferencePage).getOrElse(""),
      whatIsUrl = appConfig.contractorLandingWhatIsUrl,
      guidanceUrl = appConfig.contractorLandingGuidanceUrl,
      penaltiesUrl = appConfig.contractorLandingPenaltiesUrl
    )
}
