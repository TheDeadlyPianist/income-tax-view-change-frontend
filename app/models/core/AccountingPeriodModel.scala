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

package models.core

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate

import implicits.ImplicitDateFormatter
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.language.LanguageUtils


case class AccountingPeriodModel(start: LocalDate, end: LocalDate) {
  val determineTaxYear: Int = if(end isBefore LocalDate.of(end.getYear,4,6)) end.getYear else end.getYear + 1
}

object AccountingPeriodModel {
  implicit val format: Format[AccountingPeriodModel] = Json.format[AccountingPeriodModel]
}
