package com.hedvig.paymentservice.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.paymentservice.services.adyen.AdyenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/debug/adyen")
@Profile("development")
class AdyenDebugController(
  @Value("\${hedvig.adyen.clientKey}")
  val clientKey: String,
  val adyenService: AdyenService,
  val mapper: ObjectMapper
) {

  @GetMapping("payin")
  fun payin(
    @RequestParam("memberId")
    memberId: String
  ): ModelAndView {
    return ModelAndView(
      "adyenDebugController/index",
      mapOf<String, Any>(
        "clientKey" to clientKey,
        "memberId" to memberId,
        "paymentDirection" to "payin"
      )
    )
  }

  @GetMapping("payout")
  fun payout(
    @RequestParam("memberId")
    memberId: String
  ): ModelAndView {
    return ModelAndView(
      "adyenDebugController/index",
      mapOf<String, Any>(
        "clientKey" to clientKey,
        "memberId" to memberId,
        "paymentDirection" to "payout"
      )
    )
  }
}
