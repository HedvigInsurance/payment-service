package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market

interface MemberPayinMethodFilterService {
    fun membersWithConnectedPayinMethodForMarket(memberIds: List<String>, market: Market): List<String>
}
