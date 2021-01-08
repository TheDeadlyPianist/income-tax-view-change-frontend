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

package assets

import assets.BaseTestConstants.{testErrorMessage, testErrorStatus}
import models.financialDetails.{Charge, FinancialDetailsErrorModel, FinancialDetailsModel, SubItem}
import play.api.libs.json.{JsValue, Json}

object FinancialDetailsTestConstants {
  val testValidFinancialDetailsModelJson: JsValue = Json.obj(
    "financialDetails" -> Json.arr(
      Json.obj(
        "taxYear" -> "2019",
        "transactionId" -> "1040000123",
        "transactionDate" -> "2019-05-15",
        "type" -> "Balancing Charge Debit",
        "totalAmount" -> 12.34,
        "outstandingAmount" -> 10.33,
        "items" -> Json.arr(
          Json.obj(
            "subItemId" -> "001",
            "amount" ->  100,
            "clearingDate" -> "2019-05-15",
            "clearingReason" -> "01",
            "outgoingPaymentMethod" -> "A",
            "paymentAmount" -> 2000,
            "dueDate" -> "2019-05-15",
            "paymentMethod" -> "A",
            "paymentId" -> "081203010024-000001"
          ),
          Json.obj(
            "subItemId" -> "002",
            "amount" ->  101,
            "clearingDate" -> "2019-05-16",
            "clearingReason" -> "02",
            "outgoingPaymentMethod" -> "B",
            "paymentAmount" -> 3000,
            "dueDate" -> "2019-05-17",
            "paymentMethod" -> "B",
            "paymentId" -> "081203010025-000002"
          )
        )
      ),
      Json.obj(
        "taxYear" -> "2020",
        "transactionId" -> "1040000124",
        "transactionDate" -> "2019-05-16",
        "type" -> "Balancing Charge Debit",
        "totalAmount" -> 43.21,
        "outstandingAmount" -> 10.34,
        "items" -> Json.arr(
          Json.obj(
            "subItemId" -> "003",
            "amount" ->  110,
            "clearingDate" -> "2019-05-17",
            "clearingReason" -> "03",
            "outgoingPaymentMethod" -> "C",
            "paymentAmount" -> 5000,
            "dueDate" -> "2019-05-18",
            "paymentMethod" -> "C",
            "paymentId" -> "081203010026-000003"
          )
        )
      )
    )
  )

  val testValidFinancialDetailsModel: FinancialDetailsModel = FinancialDetailsModel(List(
    Charge(Some("2019"), Some("1040000123"), Some("2019-05-15"), Some("Balancing Charge Debit"), Some(12.34), Some(10.33),
      Some(Seq(
        SubItem(Some("001"), Some(100), Some("2019-05-15"), Some("01"), Some("A"), Some(2000), Some("2019-05-15"), Some("A"), Some("081203010024-000001")),
        SubItem(Some("002"), Some(101), Some("2019-05-16"), Some("02"), Some("B"), Some(3000), Some("2019-05-17"), Some("B"), Some("081203010025-000002"))
      ))),
    Charge(Some("2020"), Some("1040000124"), Some("2019-05-16"), Some("Balancing Charge Debit"), Some(43.21), Some(10.34),
      Some(Seq(
        SubItem(Some("003"), Some(110), Some("2019-05-17"), Some("03"), Some("C"), Some(5000), Some("2019-05-18"), Some("C"), Some("081203010026-000003"))
      )))
  ))
  val testInvalidFinancialDetailsJson: JsValue = Json.obj(
    "amount" -> "invalidAmount",
    "payMethod" -> "Payment by Card",
    "valDate" -> "2019-05-27"
  )

  val testFinancialDetailsErrorModelParsing: FinancialDetailsErrorModel = FinancialDetailsErrorModel(
    testErrorStatus, "Json Validation Error. Parsing FinancialDetails Data Response")

  val testFinancialDetailsErrorModel: FinancialDetailsErrorModel = FinancialDetailsErrorModel(testErrorStatus, testErrorMessage)
  val testFinancialDetailsErrorModelJson: JsValue = Json.obj(
    "code" -> testErrorStatus,
    "message" -> testErrorMessage
  )
}
