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

package models

import assets.PaymentAllocationChargesTestConstants.{paymentAllocationChargesModel, validPaymentAllocationChargesJson, validWrittenPaymentAllocationChargesJson}
import models.paymentAllocationCharges.PaymentAllocationChargesModel
import org.scalatest.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class PaymentsAllocationChargesModelSpec extends UnitSpec with Matchers {

  "PaymentDetails" should {

    "be formatted to JSON correctly" in {
      Json.toJson[PaymentAllocationChargesModel](paymentAllocationChargesModel) shouldBe validWrittenPaymentAllocationChargesJson
    }

    "find a correspond document detail amount to financial detail" in {
      val result = Json.fromJson[PaymentAllocationChargesModel](validPaymentAllocationChargesJson)

      result.get.filteredDocumentDetails.head.originalAmount shouldBe Some(-300)
    }

    "be able to parse a JSON into the Model" in {
      Json.fromJson[PaymentAllocationChargesModel](validPaymentAllocationChargesJson) shouldBe JsSuccess(paymentAllocationChargesModel)
    }
  }
}
