package com.hedvig.paymentservice.graphQl.types

import com.adyen.model.BrowserInfo as AdyenBrowserInfo

data class BrowserInfo(
  val userAgent: String,
  val acceptHeader: String,
  val language: String,
  val colorDepth: Int,
  val screenHeight: Int,
  val screenWidth: Int,
  val timeZoneOffset: Int,
  val javaEnabled: Boolean
) {

  companion object {
    fun toAdyenBrowserInfo(input: BrowserInfo): AdyenBrowserInfo {
      val adyenBrowserInfo = AdyenBrowserInfo()

      adyenBrowserInfo.userAgent = input.userAgent
      adyenBrowserInfo.acceptHeader = input.acceptHeader
      adyenBrowserInfo.language = input.language
      adyenBrowserInfo.colorDepth = input.colorDepth
      adyenBrowserInfo.screenHeight = input.screenHeight
      adyenBrowserInfo.screenWidth = input.screenWidth
      adyenBrowserInfo.timeZoneOffset = input.timeZoneOffset
      adyenBrowserInfo.isJavaEnabled = input.javaEnabled

      return adyenBrowserInfo
    }
  }
}
