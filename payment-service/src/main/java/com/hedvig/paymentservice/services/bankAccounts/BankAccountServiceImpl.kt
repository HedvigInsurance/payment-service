package com.hedvig.paymentservice.services.bankAccounts


import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus.Companion.fromTrustlyDirectDebitStatus
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.util.getNextChargeChargeDate
import com.hedvig.paymentservice.web.dtos.DirectDebitAccountOrderDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus as DirectDebitStatusDTO

@Service
class BankAccountServiceImpl(
    private val memberRepository: MemberRepository,
    private val accountRegistrationRepository: AccountRegistrationRepository,
    private val productPricingService: ProductPricingService,
    private val directDebitAccountOrderRepository: DirectDebitAccountOrderRepository
) : BankAccountService {

    override fun getBankAccount(memberId: String): BankAccount? {
        val directDebitAccountOrder = getLatestDirectDebitAccountOrderEntity(memberId) ?: return null

        return BankAccount.fromDirectDebitAccountOrder(directDebitAccountOrder)
    }

    override fun getNextChargeDate(memberId: String): LocalDate? {
        val hasContractActiveCurrentMonth = productPricingService.hasContractActiveCurrentMonth(memberId)
        if (!hasContractActiveCurrentMonth) {
            return null
        }
        val market = productPricingService.getContractMarketInfo(memberId).market
        return getNextChargeChargeDate(market)
    }

    override fun getDirectDebitStatus(memberId: String): DirectDebitStatusDTO {
        val accountRegistration = accountRegistrationRepository
            .findByMemberId(memberId)
            .stream()
            .max(Comparator.comparing { accountRegistration -> accountRegistration.initiated })
            .orElse(null)

        return when (getLatestDirectDebitAccountOrderEntity(memberId)?.directDebitStatus) {
            DirectDebitStatus.CONNECTED -> {
                if (accountRegistration.isNullOrConfirmedOrCancelled()) {
                    DirectDebitStatusDTO.ACTIVE
                } else {
                    DirectDebitStatusDTO.PENDING
                }
            }
            DirectDebitStatus.DISCONNECTED -> {
                if (accountRegistration.isNullOrConfirmedOrCancelled()) {
                    DirectDebitStatusDTO.NEEDS_SETUP
                } else {
                    DirectDebitStatusDTO.PENDING
                }
            }
            DirectDebitStatus.PENDING -> {
                if (accountRegistration.isNullOrCancelled()) {
                    DirectDebitStatusDTO.NEEDS_SETUP
                } else {
                    DirectDebitStatusDTO.PENDING
                }
            }
            null -> DirectDebitStatusDTO.NEEDS_SETUP
        }
    }

    override fun getPayinMethodStatus(memberId: String): PayinMethodStatus {
        val memberMaybe = memberRepository.findById(memberId)

        if (!memberMaybe.isPresent) return PayinMethodStatus.NEEDS_SETUP

        val member = memberMaybe.get()

        if (member.adyenRecurringDetailReference != null) {
            return member.payinMethodStatus
        }

        return fromTrustlyDirectDebitStatus(getDirectDebitStatus(memberId))
    }

    override fun getLatestDirectDebitAccountOrder(memberId: String): DirectDebitAccountOrderDTO? {
        val t = getLatestDirectDebitAccountOrderEntity(memberId) ?: return null
        return DirectDebitAccountOrderDTO.from(t)
    }

    private fun getLatestDirectDebitAccountOrderEntity(memberId: String): DirectDebitAccountOrder? {
        val directDebitAccountOrders = directDebitAccountOrderRepository.findAllByMemberId(memberId)
        return directDebitAccountOrders.maxByOrNull { it.createdAt }
    }

    private fun AccountRegistration?.isNullOrConfirmedOrCancelled(): Boolean = (
        this == null ||
            this.status == AccountRegistrationStatus.CONFIRMED ||
            this.status == AccountRegistrationStatus.CANCELLED
        )

    private fun AccountRegistration?.isNullOrCancelled(): Boolean = (
        this == null ||
            this.status == AccountRegistrationStatus.CANCELLED
        )

    companion object {
        private val log = LoggerFactory.getLogger(BankAccountServiceImpl::class.java)
    }
}
