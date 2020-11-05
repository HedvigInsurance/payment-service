package com.hedvig.paymentservice.web

import com.hedvig.paymentservice.services.adyen.AdyenService
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.UUID

@Controller
@RequestMapping("/debug/adyen")
@Profile("development")
class AdyenDebugController(
  val adyenService: AdyenService,
  @Value("\${hedvig.adyen.clientKey:\"\"}") val clientKey: String
) {

  @PostMapping("/payout")
  fun performPayout(
    @RequestParam("shopperReference") shopperReference: String,
    @RequestParam("memberId") memberId: String
  ): ResponseEntity<Any> {
    val response = adyenService.startPayoutTransaction(
      memberId,
      UUID.randomUUID().toString(),
      Money.of(100, "NOK"),
      shopperReference,
      "test@hedvig.com"
    )
    return ResponseEntity.ok(response)
  }

  @PostMapping("/confirmPayout")
  fun confirmPayout(
    @RequestParam("payoutReference") payoutReference: String,
    @RequestParam("memberId") memberId: String
  ): ResponseEntity<Any> {
    val response = adyenService.confirmPayout(payoutReference, memberId)
    return ResponseEntity.ok(response)
  }


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
