package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.adyenAccount.AdyenAccountRepository
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.springframework.stereotype.Service

@Service
class MemberPayinMethodFilterServiceImpl(
    private val directDebitAccountOrderRepository: DirectDebitAccountOrderRepository,
    private val adyenAccountRepository: AdyenAccountRepository
) : MemberPayinMethodFilterService {
    override fun membersWithConnectedPayinMethodForMarket(memberIds: List<String>, market: Market): List<String> {
        return when (market) {
            Market.NORWAY,
            Market.DENMARK -> {
                adyenAccountRepository.findAllByMemberIdIn(memberIds).map { it.memberId }
            }
            Market.SWEDEN -> {
                directDebitAccountOrderRepository.findAllWithLatestDirectDebitAccountOrders()
                    .filter { memberIds.contains(it.memberId) }
                    .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
                    .map { it.memberId }
            }
        }
    }

    override fun debugMembersWithConnectedPayinMethodForMarket(market: Market): List<String> =
    when (market) {
        Market.NORWAY,
        Market.DENMARK -> {
            adyenAccountRepository.findAll().map { it.memberId }
        }
        Market.SWEDEN -> {
            directDebitAccountOrderRepository.findAllWithLatestDirectDebitAccountOrders()
                .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
                .map { it.memberId }
        }
    }
}
