package com.hedvig.paymentservice.services.bankAccounts


import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus.Companion.fromTrustlyDirectDebitStatus
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.util.getNextChargeChargeDate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus as DirectDebitStatusDTO

@Service
class BankAccountServiceImpl(
    private val memberRepository: MemberRepository,
    private val accountRegistrationRepository: AccountRegistrationRepository,
    private val productPricingService: ProductPricingService
) : BankAccountService {

    override fun getBankAccount(memberId: String?): BankAccount? {
        if (memberId == null) {
            log.error("GetBankAccountInfo - hedvig.token is missing")
            throw NullPointerException("GetBankAccountInfo - hedvig.token is missing")
        }
        val optionalMember = memberRepository.findById(memberId)
        return optionalMember
            .filter { it.directDebitStatus == DirectDebitStatus.CONNECTED }
            .map { BankAccount.fromMember(it) }
            .orElse(null)
    }

    override fun getNextChargeDate(memberId: String?): LocalDate? {
        if (memberId == null) {
            log.error("GetNextChargeDate - hedvig.token is missing")
            throw NullPointerException("GetNextChargeDate - hedvig.token is missing")
        }
        val hasContractActiveCurrentMonth = productPricingService.hasContractActiveCurrentMonth(memberId)
        if (!hasContractActiveCurrentMonth) {
            return null
        }
        val market = productPricingService.getContractMarketInfo(memberId).market
        return getNextChargeChargeDate(market)
    }

    override fun getDirectDebitStatus(memberId: String?): DirectDebitStatusDTO {

        if (memberId == null) {
            log.error("GetDirectDebitStatus - hedvig.token is missing")
            throw NullPointerException("GetDirectDebitStatus - hedvig.token is missing")
        }

        val accountRegistration = accountRegistrationRepository
            .findByMemberId(memberId)
            .stream()
            .max(Comparator.comparing { accountRegistration -> accountRegistration.initiated })
            .orElse(null)

        val memberMaybe = memberRepository.findById(memberId)

        if (!memberMaybe.isPresent) return DirectDebitStatusDTO.NEEDS_SETUP

        val member = memberMaybe.get()

        return when (member.directDebitStatus) {
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

    override fun getPayinMethodStatus(memberId: String?): PayinMethodStatus {
        if (memberId == null) {
            log.error("getPayinMethodStatus - hedvig.token is missing")
            return PayinMethodStatus.NEEDS_SETUP
        }
        val memberMaybe = memberRepository.findById(memberId)
        if (memberMaybe.isPresent) {
            val member = memberMaybe.get()
            if (member.trustlyAccountNumber != null) {
                return fromTrustlyDirectDebitStatus(getDirectDebitStatus(memberId))
            }
            if (member.adyenRecurringDetailReference != null) {
                return member.payinMethodStatus
            }
        }
        return PayinMethodStatus.NEEDS_SETUP
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
