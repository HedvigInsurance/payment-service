package com.hedvig.paymentservice.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/debug/adyen")
@Profile("development")
class AdyenDebugController(
  @Value("\${hedvig.adyen.clientKey:\"\"}") val clientKey:String
) {

  @GetMapping
  fun getIndex(): ModelAndView {
    return ModelAndView("adyenDebugController/index", mapOf<String, Any>("clientKey" to clientKey))
  }

}
