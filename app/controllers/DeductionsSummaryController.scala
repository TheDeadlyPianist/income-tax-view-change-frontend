/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import audit.AuditingService
import audit.models.{AllowanceAndDeductionsRequestAuditModel, AllowanceAndDeductionsResponseAuditModel}
import auth.MtdItUser
import config.featureswitch.{DeductionBreakdown, FeatureSwitching}
import config.{FrontendAppConfig, ItvcErrorHandler, ItvcHeaderCarrierForPartialsConverter}
import controllers.predicates._
import implicits.ImplicitDateFormatter
import javax.inject.{Inject, Singleton}
import models.calculation._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{CalculationService, FinancialTransactionsService}
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.errorPages.notFound

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeductionsSummaryController @Inject()(val checkSessionTimeout: SessionTimeoutPredicate,
                                            val authenticate: AuthenticationPredicate,
                                            val retrieveNino: NinoPredicate,
                                            val retrieveIncomeSources: IncomeSourceDetailsPredicate,
                                            val calculationService: CalculationService,
                                            val itvcHeaderCarrierForPartialsConverter: ItvcHeaderCarrierForPartialsConverter,
                                            val auditingService: AuditingService,
                                            val financialTransactionsService: FinancialTransactionsService,
                                            val itvcErrorHandler: ItvcErrorHandler)
                                            (implicit val appConfig: FrontendAppConfig,
                                             mcc: MessagesControllerComponents,
                                             val executionContext: ExecutionContext,
                                             val languageUtils: LanguageUtils)
                                             extends BaseController with ImplicitDateFormatter with FeatureSwitching  with I18nSupport {

  val action: ActionBuilder[MtdItUser, AnyContent] = checkSessionTimeout andThen authenticate andThen retrieveNino andThen retrieveIncomeSources


  def showDeductionsSummary(taxYear: Int): Action[AnyContent] = {

    action.async {
      implicit user =>
        if (isEnabled(DeductionBreakdown)) {
          auditingService.extendedAudit(AllowanceAndDeductionsRequestAuditModel(user.mtditid, user.nino,
            user.saUtr, user.credId, user.userType))
          calculationService.getCalculationDetail(user.nino, taxYear).flatMap {
            case calcDisplayModel: CalcDisplayModel => {
              auditingService.extendedAudit(AllowanceAndDeductionsResponseAuditModel(user.mtditid, user.nino,
                user.saUtr, user.credId, user.userType,
                calcDisplayModel.calcDataModel.allowancesAndDeductions.personalAllowance,
                calcDisplayModel.calcDataModel.allowancesAndDeductions.totalPensionContributions))

              Future.successful(Ok(views.html.deductionBreakdown(calcDisplayModel, taxYear)))
            }
            case CalcDisplayNoDataFound =>
              Logger.warn(s"[DeductionsSummaryController][showDeductionsSummary[$taxYear]] No deductions data could be retrieved. Not found")
              Future.successful(itvcErrorHandler.showInternalServerError())

            case CalcDisplayError =>
              Logger.error(s"[DeductionsSummaryController][showDeductionsSummary[$taxYear]] No deductions data could be retrieved. Downstream error")
              Future.successful(itvcErrorHandler.showInternalServerError())
          }
        }
        else Future.successful(NotFound(notFound()))
    }
  }

}
