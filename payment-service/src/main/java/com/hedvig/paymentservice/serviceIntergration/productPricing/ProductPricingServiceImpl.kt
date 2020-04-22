package com.hedvig.paymentservice.serviceIntergration.productPricing

import com.hedvig.paymentservice.query.member.entities.Transaction
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.MarketInfo
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

  override fun getInsuranceStatus(memberId: String?): Optional<InsuranceStatus> {
    try {
      val response =
        client.getInsuranceStatus(memberId)
      return if (response.statusCode.is2xxSuccessful) Optional.ofNullable(response.body) else Optional.empty()
    } catch (ex: FeignException) {
      when (ex.status()) {
        500 -> {
          logger.error("Product-pricing returned 500 response")
        }
      }
    }
    return Optional.empty()
  }

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

  override fun getMarketInfo(memberId: String): MarketInfo {
    return client.getMarketInfo(memberId).body!!
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }
}
