/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import audit.AuditingService
import audit.models.{WhatYouOweRequestAuditModel, WhatYouOweResponseAuditModel}
import config.featureswitch.{FeatureSwitching, NewFinancialDetailsApi, TxmEventsApproved}
import config.{FrontendAppConfig, ItvcErrorHandler, ItvcHeaderCarrierForPartialsConverter}
import controllers.predicates.{AuthenticationPredicate, IncomeSourceDetailsPredicate, NinoPredicate, SessionTimeoutPredicate}
import forms.utils.SessionKeys
import implicits.{ImplicitDateFormatter, ImplicitDateFormatterImpl}
import models.financialDetails._
import models.financialTransactions.{FinancialTransactionsErrorModel, FinancialTransactionsModel, FinancialTransactionsResponseModel}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import services.{FinancialTransactionsService, PaymentDueService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import javax.inject.Inject

import scala.concurrent.ExecutionContext

class PaymentDueController @Inject()(val checkSessionTimeout: SessionTimeoutPredicate,
                                     val authenticate: AuthenticationPredicate,
                                     val retrieveNino: NinoPredicate,
                                     val retrieveIncomeSources: IncomeSourceDetailsPredicate,
                                     val financialTransactionsService: FinancialTransactionsService,
                                     val paymentDueService: PaymentDueService,
                                     val itvcHeaderCarrierForPartialsConverter: ItvcHeaderCarrierForPartialsConverter,
                                     val itvcErrorHandler: ItvcErrorHandler,
                                     val auditingService: AuditingService,
                                     implicit val appConfig: FrontendAppConfig,
                                     mcc: MessagesControllerComponents,
                                     implicit val ec: ExecutionContext,
                                     dateFormatter: ImplicitDateFormatterImpl
                                    ) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  private def view(chargesList: WhatYouOweChargesList,  currentTaxYear: Int, implicitDateFormatter: ImplicitDateFormatter, backUrl: String, utr: Option[String],
                   latePaymentInterestCharge: Boolean)(implicit request: Request[_]) = {
    views.html.whatYouOwe(chargesList, currentTaxYear, implicitDateFormatter, backUrl, utr, latePaymentInterestCharge)
  }


  def hasFinancialTransactionsError(transactionModels: List[FinancialTransactionsResponseModel]): Boolean = {
    transactionModels.exists(_.isInstanceOf[FinancialTransactionsErrorModel])
  }

  def hasFinancialDetailsError(financialDetails: List[FinancialDetailsResponseModel]): Boolean = {
    financialDetails.exists(_.isInstanceOf[FinancialDetailsErrorModel])
  }


  def viewPaymentsDue(isLatePaymentCharge: Boolean = false): Action[AnyContent] =
    (checkSessionTimeout andThen authenticate andThen retrieveNino andThen retrieveIncomeSources).async {
    implicit user =>
      if (isEnabled(NewFinancialDetailsApi) && !isLatePaymentCharge) {
        if (isEnabled(TxmEventsApproved)) {
          auditingService.extendedAudit(WhatYouOweRequestAuditModel(user))
        }

        paymentDueService.getWhatYouOweChargesList().map {
          whatYouOweChargesList =>
            if (isEnabled(TxmEventsApproved)) {
              auditingService.extendedAudit(WhatYouOweResponseAuditModel(user, whatYouOweChargesList))
            }

            Ok(view(chargesList = whatYouOweChargesList,currentTaxYear = user.incomeSources.getCurrentTaxEndYear,
               implicitDateFormatter = dateFormatter, backUrl = backUrl, user.saUtr, latePaymentInterestCharge = isLatePaymentCharge)
            ).addingToSession(SessionKeys.chargeSummaryBackPage -> "paymentDue")
        } recover {
          case ex: Exception =>
            Logger.error(s"Error received while getting what you page details: ${ex.getMessage}")
            itvcErrorHandler.showInternalServerError()
        }
      } else {
        financialTransactionsService.getAllUnpaidFinancialTransactions.map {
          case transactions if hasFinancialTransactionsError(transactions) => itvcErrorHandler.showInternalServerError()
          case transactions: List[FinancialTransactionsModel] => Ok(views.html.paymentDue(financialTransactions = transactions,
             backUrl = backUrl, implicitDateFormatter = dateFormatter)
          ).addingToSession(SessionKeys.chargeSummaryBackPage -> "paymentDue")
        }
      }
  }


  lazy val backUrl: String = controllers.routes.HomeController.home().url

}
