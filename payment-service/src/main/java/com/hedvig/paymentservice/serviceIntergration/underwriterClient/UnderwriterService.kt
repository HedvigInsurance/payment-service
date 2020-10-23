package com.hedvig.paymentservice.serviceIntergration.underwriterClient

import com.hedvig.paymentservice.serviceIntergration.underwriterClient.dtos.QuoteMarketInfo

interface UnderwriterService {
  fun getMarketFromQuote(memberId: String): QuoteMarketInfo
}
