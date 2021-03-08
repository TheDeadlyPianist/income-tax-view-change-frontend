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

import assets.FinancialDetailsTestConstants._
import assets.FinancialTransactionsTestConstants._
import audit.AuditingService
import config.featureswitch.{FeatureSwitching, NewFinancialDetailsApi}
import config.{FrontendAppConfig, ItvcErrorHandler, ItvcHeaderCarrierForPartialsConverter}
import controllers.predicates.{NinoPredicate, SessionTimeoutPredicate}
import implicits.{ImplicitDateFormatter, ImplicitDateFormatterImpl}
import mocks.controllers.predicates.{MockAuthenticationPredicate, MockIncomeSourceDetailsPredicate}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import services.{FinancialDetailsService, FinancialTransactionsService}

import scala.concurrent.Future

class PaymentDueControllerSpec extends MockAuthenticationPredicate
  with MockIncomeSourceDetailsPredicate with ImplicitDateFormatter with FeatureSwitching {


  trait Setup {

    val financialTransactionsService: FinancialTransactionsService = mock[FinancialTransactionsService]
    val financialDetailsService: FinancialDetailsService = mock[FinancialDetailsService]

    val controller = new PaymentDueController(
      app.injector.instanceOf[SessionTimeoutPredicate],
      MockAuthenticationPredicate,
      app.injector.instanceOf[NinoPredicate],
      MockIncomeSourceDetailsPredicate,
      financialTransactionsService,
      financialDetailsService,
      app.injector.instanceOf[ItvcHeaderCarrierForPartialsConverter],
      app.injector.instanceOf[ItvcErrorHandler],
      app.injector.instanceOf[AuditingService],
      app.injector.instanceOf[FrontendAppConfig],
      app.injector.instanceOf[MessagesControllerComponents],
      ec,
      app.injector.instanceOf[ImplicitDateFormatterImpl]
    )
  }

  def testFinancialTransaction(taxYear: Int) = financialTransactionsModel(s"$taxYear-04-05")
  def testFinancialDetail(taxYear: Int) = financialDetailsModel(taxYear)

  val noFinancialTransactionErrors = List(testFinancialTransaction(2018))
  val hasFinancialTransactionErrors = List(testFinancialTransaction(2018), financialTransactionsErrorModel)
  val hasAFinancialTransactionError = List(financialTransactionsErrorModel)

  val noFinancialDetailErrors = List(testFinancialDetail(2018))
  val hasFinancialDetailErrors = List(testFinancialDetail(2018), testFinancialDetailsErrorModel)
  val hasAFinancialDetailError = List(testFinancialDetailsErrorModel)

  "The PaymentDueControllerSpec.hasFinancialTransactionsError function" when {
    "checking the list of transactions" should {
      "produce false if there are no errors are present" in new Setup {
        val result = controller.hasFinancialTransactionsError(noFinancialTransactionErrors)
        result shouldBe false
      }
      "produce true if any errors are present" in new Setup {
        val result = controller.hasFinancialTransactionsError(hasFinancialTransactionErrors)
        result shouldBe true
      }
    }
  }


  "The PaymentDueControllerSpec.viewPaymentsDue function" when {
    "NewFinancialDetailsApi FS is disbaled" when {
      disable(NewFinancialDetailsApi)
      "obtaining a users transaction" should {
        "send the user to the paymentsDue page with transactions" in new Setup {
          mockSingleBusinessIncomeSource()
          when(financialTransactionsService.getAllUnpaidFinancialTransactions(any(), any(), any()))
            .thenReturn(Future.successful(noFinancialTransactionErrors))


          val result = await(controller.viewPaymentsDue(fakeRequestWithActiveSession))

          status(result) shouldBe Status.OK
        }

        "send the user to the Internal error page with internal server errors" in new Setup {
          mockSingleBusinessIncomeSource()
          when(financialTransactionsService.getAllUnpaidFinancialTransactions(any(), any(), any()))
            .thenReturn(Future.successful(hasAFinancialTransactionError))

          val result = await(controller.viewPaymentsDue(fakeRequestWithActiveSession))

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "send the user to the Internal error page with internal server errors and transactions" in new Setup {
          mockBothIncomeSources()
          when(financialTransactionsService.getAllUnpaidFinancialTransactions(any(), any(), any()))
            .thenReturn(Future.successful(hasFinancialTransactionErrors))

          val result = controller.viewPaymentsDue(fakeRequestWithActiveSession)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        }
      }
    }
    "NewFinancialDetailsApi FS is enabled" when {
      "obtaining a users charge" should {
        "send the user to the paymentsOwe page with charges with YearOfMigration data existing" in new Setup {
          enable(NewFinancialDetailsApi)
          mockSingleBusinessIncomeSource()
          when(financialDetailsService.getFinancialDetails(any())(any(), any()))
            .thenReturn(Future.successful(testFinancialDetail(2017)))

          val result = await(controller.viewPaymentsDue(fakeRequestWithActiveSession))

          status(result) shouldBe Status.OK
        }

        "send the user to the Internal error page with financialDetails errors and YearOfMigration data existing" in new Setup {
          enable(NewFinancialDetailsApi)
          mockSingleBusinessIncomeSource()
          when(financialDetailsService.getFinancialDetails(any())(any(), any()))
            .thenReturn(Future.successful(testFinancialDetailsErrorModel))

          val result = await(controller.viewPaymentsDue(fakeRequestWithActiveSession))

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "send the user to the Internal error page with YearOfMigration data not existing" in new Setup {
          enable(NewFinancialDetailsApi)
          mockPropertyIncomeSource()

          val result = await(controller.viewPaymentsDue(fakeRequestWithActiveSession))

          status(result) shouldBe Status.OK
        }

        "send the user to the Internal error page when Income sources call fails" in new Setup {
          enable(NewFinancialDetailsApi)
          mockErrorIncomeSource()

          val result = controller.viewPaymentsDue(fakeRequestWithActiveSession)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR

        }
      }
      disable(NewFinancialDetailsApi)
    }
  }

}
