/*
 * Copyright 2018 HM Revenue & Customs
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

package assets

import assets.BaseTestConstants.{testErrorMessage, testErrorStatus}
import assets.ReportDeadlinesTestConstants.obligationsDataSuccessModel
import models.{AccountingPeriodModel, PropertyDetailsErrorModel, PropertyDetailsModel, PropertyIncomeModel}
import utils.ImplicitDateFormatter._

object PropertyDetailsTestConstants {
  val propertyIncomeModel = PropertyIncomeModel(
    accountingPeriod = AccountingPeriodModel("2017-04-06", "2018-04-05"),
    obligationsDataSuccessModel
  )
  val testPropertyAccountingPeriod = AccountingPeriodModel(start = "2017-4-6", end = "2018-4-5")
  val propertySuccessModel = PropertyDetailsModel(testPropertyAccountingPeriod)
  val propertyErrorModel = PropertyDetailsErrorModel(testErrorStatus, testErrorMessage)

}