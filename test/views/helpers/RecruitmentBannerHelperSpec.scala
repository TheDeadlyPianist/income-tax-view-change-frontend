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

package views.helpers

import assets.MessagesLookUp
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import testUtils.TestSupport

class RecruitmentBannerHelperSpec extends TestSupport{

  "The recruitment banner" should {

    lazy val view = views.html.helpers.recruitmentBannerHelper()
    lazy val document = Jsoup.parse(view.body)

    s"have the correct header message '${MessagesLookUp.RecruitmentBanner.text}'" in {
      document.getElementById("recruitment-banner-text").text shouldBe MessagesLookUp.RecruitmentBanner.text
    }
    s"have the correct link text '${MessagesLookUp.RecruitmentBanner.link}' and url ${appConfig.enterSurveyUrl}" in {
      document.getElementById("recruitment-banner-link").text shouldBe MessagesLookUp.RecruitmentBanner.link
      document.getElementById("recruitment-banner-link").attr("href") shouldBe appConfig.enterSurveyUrl
    }
    s"have the correct dismiss text '${MessagesLookUp.RecruitmentBanner.dismiss}'" in {
      document.getElementById("recruitment-banner-dismiss").text shouldBe MessagesLookUp.RecruitmentBanner.dismiss
    }

  }

}
