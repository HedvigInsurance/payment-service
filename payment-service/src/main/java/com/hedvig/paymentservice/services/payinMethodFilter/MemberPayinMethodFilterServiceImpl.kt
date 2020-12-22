package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
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
                directDebitAccountOrderRepository.findAllWithLatestDirectDebitAccountOrders(memberIds)
                    .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
                    .map { it.memberId }
            }
        }
    }

    override fun debugMembersWithConnectedPayinMethodForMarket(market: Market): List<String> =
    when (market) {
        Market.NORWAY,
        Market.DENMARK -> {
            val members = memberRepository.findAll()
            members.filter { it.adyenRecurringDetailReference != null }.map { it.id }
        }
        Market.SWEDEN -> {
            val memberIds = memberRepository.findAll().map { member -> member.id }
            directDebitAccountOrderRepository.findAllWithLatestDirectDebitAccountOrders(memberIds)
                .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
                .map { it.memberId }
        }
    }
}
