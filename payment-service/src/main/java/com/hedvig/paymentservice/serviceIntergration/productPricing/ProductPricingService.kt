package com.hedvig.paymentservice.serviceIntergration.productPricing

import com.hedvig.paymentservice.query.member.entities.Transaction
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.MarketInfo
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto
import java.time.YearMonth
import java.util.Optional
import java.util.UUID

interface ProductPricingService {
  fun getInsuranceStatus(memberId: String?): Optional<InsuranceStatus>
  
  fun guessPolicyTypes(
    transactions: Collection<Transaction>,
    period: YearMonth
  ): Map<UUID, Optional<PolicyGuessResponseDto>>

  fun getMarketInfo(memberId: String): MarketInfo
}
