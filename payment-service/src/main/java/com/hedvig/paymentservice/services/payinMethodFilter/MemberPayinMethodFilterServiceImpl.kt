package com.hedvig.paymentservice.services.payinMethodFilter

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.adyenAccount.MemberAdyenAccountRepository
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.springframework.stereotype.Service

@Service
class MemberPayinMethodFilterServiceImpl(
    private val directDebitAccountOrderRepository: DirectDebitAccountOrderRepository,
    private val memberAdyenAccountRepository: MemberAdyenAccountRepository
) : MemberPayinMethodFilterService {
    override fun membersWithConnectedPayinMethodForMarket(memberIds: List<String>, market: Market): List<String> {
        return when (market) {
            Market.NORWAY,
            Market.DENMARK -> {
                memberAdyenAccountRepository
                    .findAllByMemberIdIn(memberIds)
                    .filter { it.recurringDetailReference != null }
                    .map { it.memberId }
            }
            Market.SWEDEN -> {
                directDebitAccountOrderRepository.findAllWithLatestDirectDebitAccountOrders()
                    .filter { memberIds.contains(it.memberId) }
                    .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
                    .map { it.memberId }
            }
        }
    }
}
