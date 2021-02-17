package com.hedvig.paymentservice.services.swish.client

import com.hedvig.paymentservice.services.swish.PayoutRequest
import com.hedvig.paymentservice.services.swish.SwishService
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "swishClient",
    url = "\${hedvig.external.swish.baseurl:https://staging.getswish.pub.tds.tieto.com/cpc-swish/}",
    configuration = [SwishFeignConfiguration::class]
)
interface SwishClient {
    @PostMapping("/api/v1/payouts/")
    fun payout(
        @RequestBody payloadRequest: PayoutRequest
    ): ResponseEntity<*>
}
