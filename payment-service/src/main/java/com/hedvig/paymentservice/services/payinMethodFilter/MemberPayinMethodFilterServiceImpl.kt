package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.springframework.stereotype.Service

@Service
class MemberPayinMethodFilterServiceImpl(
    private val memberRepository: MemberRepository,
    private val directDebitAccountOrderRepository: DirectDebitAccountOrderRepository
) : MemberPayinMethodFilterService {
    override fun membersWithConnectedPayinMethodForMarket(memberIds: List<String>, market: Market): List<String> {
        return when (market) {
            Market.NORWAY,
            Market.DENMARK -> {
                val members = memberRepository.findAllByIdIn(memberIds)
                if (members.isEmpty()) return emptyList()
                members.filter { it.adyenRecurringDetailReference != null }.map { it.id }
            }
            Market.SWEDEN -> {
                memberIds.mapNotNull {
                    directDebitAccountOrderRepository.findFirstByMemberIdOrderByCreatedAtDesc(it)?.memberId
                }
            }
        }
    }
}
