package com.hedvig.paymentservice.query.adyenAccount

import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.AdyenTokenRegistrationAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTokenRegistration.events.PendingAdyenTokenRegistrationCreatedEvent
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("MemberAdyenAccount")
class MemberAdyenAccountEventListener(
    private val memberAdyenAccountRepository: MemberAdyenAccountRepository
) {

    @EventHandler
    fun on(event: AdyenTokenRegistrationAuthorisedEvent) {
        logger.info("AdyenTokenRegistrationAuthorisedEvent - Account created/updated [MemberId: ${event.memberId}] [MerchantAccount: ${event.adyenMerchantAccount}]")
        createOrUpdateMemberAdyenAccountWithMerchantInfo(event.memberId, event.adyenMerchantAccount)
    }

    @EventHandler
    fun on(event: PendingAdyenTokenRegistrationCreatedEvent) {
        logger.info("PendingAdyenTokenRegistrationCreatedEvent - Account created/updated [MemberId: ${event.memberId}] [MerchantAccount: ${event.adyenMerchantAccount}]")
        createOrUpdateMemberAdyenAccountWithMerchantInfo(event.memberId, event.adyenMerchantAccount)
    }

    @EventHandler
    fun on(event: AdyenAccountCreatedEvent) {
        logger.info("AdyenAccountCreatedEvent - [MemberId: ${event.memberId}] [Reference: ${event.recurringDetailReference}]")
        updateAndSave(event.memberId, event.recurringDetailReference, event.accountStatus)
    }

    @EventHandler
    fun on(event: AdyenAccountUpdatedEvent) {
        logger.info("AdyenAccountUpdatedEvent - [MemberId: ${event.memberId}] [Reference: ${event.recurringDetailReference}]")
        updateAndSave(event.memberId, event.recurringDetailReference, event.accountStatus)
    }

    private fun createOrUpdateMemberAdyenAccountWithMerchantInfo(memberId: String, merchantAccount: String?) {
        val accountMaybe = memberAdyenAccountRepository.findById(memberId)

        logger.info("CreateMerchantInfo - [MemberId: $memberId] [Account: $merchantAccount]")

        if (accountMaybe.isPresent) {
            val account = accountMaybe.get()
            account.merchantAccount = merchantAccount ?: HEDVIG_ABCOM
            memberAdyenAccountRepository.save(account)
            logger.info("Account updated - [MemberId: $memberId] [Account: ${account.merchantAccount}]")
        } else {
            memberAdyenAccountRepository.save(
                MemberAdyenAccount(
                    memberId = memberId,
                    merchantAccount = merchantAccount ?: HEDVIG_ABCOM
                )
            )
            logger.info("Account created - [MemberId: $memberId] [Account: ${merchantAccount ?: HEDVIG_ABCOM}]")
        }
    }

    private fun updateAndSave(
        memberId: String,
        recurringDetailReference: String,
        accountStatus: AdyenAccountStatus
    ) {
        val adyenAccountMaybe = memberAdyenAccountRepository.findById(memberId)

        if (!adyenAccountMaybe.isPresent) {
            throw IllegalStateException(
                "Cannot find adyen account for member $memberId " +
                    "[Reference: $recurringDetailReference] [Status: $accountStatus]"
            )
        }

        val adyenAccountToUpdate = adyenAccountMaybe.get()

        adyenAccountToUpdate.recurringDetailReference = recurringDetailReference
        adyenAccountToUpdate.accountStatus = accountStatus

        memberAdyenAccountRepository.save(adyenAccountToUpdate)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
        const val HEDVIG_ABCOM = "HedvigABCOM"
    }
}
