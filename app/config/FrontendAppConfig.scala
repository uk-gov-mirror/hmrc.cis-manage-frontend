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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val host: String    = configuration.get[String]("host")
  lazy val appName: String = configuration.get[String]("appName")

  private lazy val contactHost                  = configuration.get[String]("contact-frontend.host")
  private lazy val contactFormServiceIdentifier = configuration.get[String]("contact-frontend.serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  lazy val loginUrl: String                                   = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String                           = configuration.get[String]("urls.loginContinue")
  lazy val signOutUrl: String                                 = configuration.get[String]("urls.signOut")
  lazy val govUkCISGuidanceUrl: String                        = configuration.get[String]("urls.govUkCISGuidance")
  lazy val commercialSoftwareSuppliersUrl: String             = configuration.get[String]("urls.commercialSoftwareSuppliers")
  lazy val cisContractorsGuidanceUrl: String                  = configuration.get[String]("urls.cisContractorsGuidance")
  lazy val cisUpdatesUrl: String                              = configuration.get[String]("urls.cisUpdates")
  lazy val cisMoreInformationUrl: String                      = configuration.get[String]("urls.cisMoreInformation")
  lazy val technicalSupportWithHmrcOnlineServicesUrl: String  =
    configuration.get[String]("urls.technicalSupportWithHmrcOnlineServices")
  lazy val registerAsAProfessionalTaxAgentWithHmrcUrl: String =
    configuration.get[String]("urls.registerAsAProfessionalTaxAgentWithHmrc")
  lazy val taxAgentsAndAdvisorsAuthorisationFormsUrl: String  =
    configuration.get[String]("urls.taxAgentsAndAdvisorsAuthorisationForms")
  lazy val returnToHomeUrl: String                            = configuration.get[String]("urls.returnToHome")
  lazy val cisHelpWhatIsUrl: String                           = configuration.get[String]("urls.cis-help-what-is")
  lazy val cisHelpMonthlyUrl: String                          = configuration.get[String]("urls.cis-help-monthly")
  lazy val cisHelp340Url: String                              = configuration.get[String]("urls.cis-help-340")
  lazy val cisHelpWhatYouMustDoAsContractor: String           =
    configuration.get[String]("urls.cis-help-what-you-must-do-as-a-contractor")
  lazy val hmrcOnlineServiceDeskUrl: String                   = configuration.get[String]("urls.hmrcOnlineServiceDesk")
  lazy val payeCisForAgentsOnlineService: String              = configuration.get[String]("urls.payeCisForAgentsOnlineService")
  lazy val cisMonthlyReturnsGuidanceUrl: String               = configuration.get[String]("urls.cisMonthlyReturnsGuidance")
  lazy val cis340PenaltiesForLateReturnsUrl: String           = configuration.get[String]("urls.cis340PenaltiesForLateReturns")
  lazy val cisLateFilingPenaltyUrl: String                    = configuration.get[String]("urls.cisLateFilingPenalty")
  lazy val cisSubcontractorGrossPaymentStatusUrl: String      =
    configuration.get[String]("urls.cisSubcontractorGrossPaymentStatus")
  lazy val amendReturnUrl: String                             = configuration.get[String]("urls.amendReturn")
  lazy val paymentsAndDeductionsUrl: String                   = configuration.get[String]("urls.paymentsAndDeductions")
  lazy val noticesAndStatementsUrl: String                    = configuration.get[String]("urls.noticesAndStatements")
  lazy val viewReturnsHistoryUrl: String                      = configuration.get[String]("urls.viewReturnsHistory")
  lazy val hmrcContactCISUrl: String                          = configuration.get[String]("urls.hmrcContactCIS")
  private val exitSurveyBaseUrl: String                       = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  lazy val exitSurveyUrl: String                              = s"$exitSurveyBaseUrl/feedback/cis-manage-frontend"

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  lazy val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  lazy val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val contractorLandingWhatIsUrl: String    = configuration.get[String]("urls.cisGuideUrl")
  lazy val contractorLandingGuidanceUrl: String  = configuration.get[String]("urls.cisPayEmployerPayeUrl")
  lazy val contractorLandingPenaltiesUrl: String = configuration.get[String]("urls.cisCheckPenaltiesUrl")

  private lazy val cisFrontendBaseUrl: String                       = configuration.get[String]("cis-frontend.host")
  private lazy val portalAccountBaseUrl: String                     = configuration.get[String]("portal-account.host")
  private lazy val fileStandardReturnPath: String                   = configuration.get[String]("urls.fileStandardReturn")
  private lazy val fileNilReturnPath: String                        = configuration.get[String]("urls.fileNilReturn")
  private lazy val continueReturnJourneyPath: String                = configuration.get[String]("urls.continueReturnJourney")
  private lazy val continueAmendReturnJourneyPath: String           = configuration.get[String]("urls.continueAmendReturnJourney")
  private lazy val confirmAmendmentPath: String                     = configuration.get[String]("urls.confirmAmendment")
  private lazy val submissionInProgressPath: String                 = configuration.get[String]("urls.submissionInProgress")
  private lazy val submissionUnsuccessfulCannotResubmitPath: String =
    configuration.get[String]("urls.submissionUnsuccessfulCannotResubmit")
  private lazy val authoriseClientRequestPath: String               = configuration.get[String]("urls.authoriseClientRequest")

  def fileStandardReturnUrl: String = s"$cisFrontendBaseUrl$fileStandardReturnPath"

  def fileStandardReturnUrl(instanceId: String): String = {
    def encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
    s"$cisFrontendBaseUrl$fileStandardReturnPath" +
      s"?instanceId=${encode(instanceId)}"
  }

  def fileNilReturnUrl: String = s"$cisFrontendBaseUrl$fileNilReturnPath"

  def fileNilReturnUrl(instanceId: String): String = {
    def encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
    s"$cisFrontendBaseUrl$fileNilReturnPath" +
      s"?instanceId=${encode(instanceId)}"
  }

  def continueReturnJourneyUrl(instanceId: String, taxYear: String, taxMonth: String): String =
    s"$cisFrontendBaseUrl$continueReturnJourneyPath" +
      s"?instanceId=$instanceId&taxYear=$taxYear&taxMonth=$taxMonth"

  def continueAmendReturnJourneyUrl(
    instanceId: String,
    taxYear: String,
    taxMonth: String,
    isOriginalNilReturn: Boolean
  ): String =
    s"$cisFrontendBaseUrl$continueAmendReturnJourneyPath" +
      s"?instanceId=$instanceId&taxYear=$taxYear&taxMonth=$taxMonth&isOriginalNilReturn=$isOriginalNilReturn"

  def confirmAmendmentUrl(handoffId: String): String =
    s"$cisFrontendBaseUrl$confirmAmendmentPath?handoffId=$handoffId"

  def submissionInProgressUrl(cisId: String): String = s"$cisFrontendBaseUrl$submissionInProgressPath?cisId=$cisId"

  def submissionUnsuccessfulCannotResubmitUrl(cisId: String): String =
    s"$cisFrontendBaseUrl$submissionUnsuccessfulCannotResubmitPath?cisId=$cisId"

  def authoriseClientRequestUrl(agentCode: String): String =
    s"$portalAccountBaseUrl${authoriseClientRequestPath.replace("{agentCode}", agentCode)}"

  private val cisContractorFrontendBaseUrl: String  =
    configuration.get[Service]("microservice.services.cis-contractor-frontend").baseUrl
  lazy val cisTypeOfSubcontractorUrl: String        =
    s"$cisContractorFrontendBaseUrl${configuration.get[String]("urls.cis-contractor-frontend")}"
  lazy val cisVerifySubcontractorUrl: String        =
    s"$cisContractorFrontendBaseUrl${configuration.get[String]("urls.cis-contractor-frontend")}/verify/newest"
  lazy val contractorDetailsIntroductionUrl: String =
    s"$cisContractorFrontendBaseUrl${configuration.get[String]("urls.contractorDetailsIntroduction")}"
  lazy val contractorDetailsManagementUrl: String   =
    s"$cisContractorFrontendBaseUrl${configuration.get[String]("urls.contractorDetailsManagement")}"

}
