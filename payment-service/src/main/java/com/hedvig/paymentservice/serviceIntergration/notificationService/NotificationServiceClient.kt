package com.hedvig.paymentservice.serviceIntergration.notificationService

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient("NotificationServiceClient", url = "\${hedvig.notification-service.url:notification-service}")
interface NotificationServiceClient {

  @PostMapping("/_/customerio/{memberId}")
  fun updateCustomer(@PathVariable memberId: String, @RequestBody data: JsonNode)
}
