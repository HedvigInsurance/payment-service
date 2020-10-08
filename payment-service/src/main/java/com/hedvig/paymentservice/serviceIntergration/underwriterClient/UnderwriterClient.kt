package com.hedvig.paymentservice.serviceIntergration.underwriterClient

import com.hedvig.paymentservice.serviceIntergration.underwriterClient.dtos.QuoteMarketInfo
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
  name = "underwriterClient",
  url = "\${hedvig.underwriter.url:underwriter}"
)
interface UnderwriterClient {
  @GetMapping("/_/v1/quotes/members/{memberId}/latestQuote/marketInfo")
  fun getQuoteMarketInfoFromMemberId(@PathVariable memberId: String): ResponseEntity<QuoteMarketInfo>
}
