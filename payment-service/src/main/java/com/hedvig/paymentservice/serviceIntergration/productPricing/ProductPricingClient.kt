package com.hedvig.paymentservice.serviceIntergration.productPricing

import com.hedvig.paymentservice.configuration.FeignConfiguration
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.time.YearMonth
import java.util.Optional
import java.util.UUID

@FeignClient(
  name = "productPricingClient",
  url = "\${hedvig.product-pricing.url:product-pricing}",
  configuration = [FeignConfiguration::class]
)
interface ProductPricingClient {

  @PostMapping(path = ["/report/policies/guess-types/{period}"])
  fun guessPolicyTypes(
    @RequestBody policiesToGuesses: Collection<PolicyGuessRequestDto>,
    @RequestParam("period") period: YearMonth
  ): ResponseEntity<Map<UUID, Optional<PolicyGuessResponseDto>>>

  @GetMapping(path = ["/_/contracts/members/{memberId}/contract/market/info"])
  fun getContractMarketInfo(@RequestParam memberId: String): ResponseEntity<ContractMarketInfo>
}
