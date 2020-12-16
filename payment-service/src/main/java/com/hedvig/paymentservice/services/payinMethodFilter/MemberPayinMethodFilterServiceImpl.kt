package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.springframework.stereotype.Service

@Service
class MemberPayinMethodFilterServiceImpl(
    private val memberRepository: MemberRepository
) : MemberPayinMethodFilterService {

    override fun membersWithConnectedPayinMethodForMarket(memberIds: List<String>, market: Market): List<String> {
        val members = memberRepository.findAllByIdIn(memberIds)

        if (members.isEmpty()) return emptyList()

        return members.filter { member ->
            when (market) {
                Market.NORWAY,
                Market.DENMARK -> member.adyenRecurringDetailReference != null
                Market.SWEDEN -> member.directDebitStatus == DirectDebitStatus.CONNECTED
                    && member.trustlyAccountNumber != null
            }
        }.map { member -> member.id }
    }
}