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

  @GetMapping
  fun getIndex(@RequestParam memberId: String): ModelAndView {
    return ModelAndView(
      "adyenDebugController/index",
      mapOf<String, Any>("clientKey" to clientKey, "memberId" to memberId)
    )
  }

  @PostMapping("/payout")
  fun performPayout(shopperReference: String): ResponseEntity<Any> {
    val response = adyenService.startPayoutTransaction(
      UUID.randomUUID().toString(),
      Money.of(100, "NOK"),
      shopperReference,
      "test@hedvig.com"
    )
    return ResponseEntity.ok(response)
  }
}
