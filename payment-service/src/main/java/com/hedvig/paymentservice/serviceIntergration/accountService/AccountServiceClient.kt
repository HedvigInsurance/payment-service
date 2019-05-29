package com.hedvig.paymentservice.serviceIntergration.accountService

import com.hedvig.paymentservice.serviceIntergration.accountService.dto.NotifyChargeCompletedRequestDto
import com.hedvig.paymentservice.serviceIntergration.accountService.dto.NotifyChargeFailedRequestDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
  name = "account-service",
  url = "\${hedvig.account-service.url:account-service}"
)
interface AccountServiceClient {
  @PostMapping("/_/schedule/charge/{memberId}/failed")
  fun notifyChargeFailed(
    @PathVariable memberId: String,
    @RequestBody request: NotifyChargeFailedRequestDto
  ): ResponseEntity<Void>

  @PostMapping("/_/schedule/charge/{memberId}/completed")
  fun notifyChargeCompleted(
    @PathVariable memberId: String,
    @RequestBody request: NotifyChargeCompletedRequestDto
  ): ResponseEntity<Void>
}
