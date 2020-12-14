package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market

interface MemberPayinFilterService {
    fun membersWithConnectedPayinMethodForMarket(market: Market): List<String>
}
