package com.hedvig.paymentservice.serviceIntergration.productPricing

import com.hedvig.paymentservice.query.member.entities.Transaction
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.YearMonth
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors

@Component
class ProductPricingServiceImpl(
  private val client: ProductPricingClient
) : ProductPricingService {

  override fun guessPolicyTypes(
    transactions: Collection<Transaction>,
    period: YearMonth
  ): Map<UUID, Optional<PolicyGuessResponseDto>> {
    val policyGuessDtos: Collection<PolicyGuessRequestDto> = transactions.stream()
      .map { transaction: Transaction ->
        PolicyGuessRequestDto.from(transaction)
      }
      .collect(Collectors.toList())
    return client.guessPolicyTypes(policyGuessDtos, period).body!!
  }

  override fun getContractMarketInfo(memberId: String): ContractMarketInfo {
    return client.getContractMarketInfo(memberId).body!!
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }
}
