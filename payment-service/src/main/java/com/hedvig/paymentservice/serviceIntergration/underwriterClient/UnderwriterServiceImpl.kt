package com.hedvig.paymentservice.serviceIntergration.underwriterClient

import com.hedvig.paymentservice.serviceIntergration.underwriterClient.dtos.QuoteMarketInfo
import org.springframework.stereotype.Service

@Service
class UnderwriterServiceImpl(
    val underwriterClient: UnderwriterClient
) : UnderwriterService {
  override fun getMarketFromQuote(memberId: String): QuoteMarketInfo {
    return underwriterClient.getQuoteMarketInfoFromMemberId(memberId).body!!
  }
}
