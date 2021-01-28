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
@ProcessingGroup("AdyenAccount")
class AdyenAccountEventListener(
    private val adyenAccountRepository: AdyenAccountRepository
) {

    @EventHandler
    fun on(event: AdyenTokenRegistrationAuthorisedEvent) {
        logger.info("AdyenTokenRegistrationAuthorisedEvent - Account created/updated [MemberId: ${event.memberId}] [MerchantAccount: ${event.adyenMerchantAccount}]")
        createOrUpdateAdyenAccountWithMerchantInfo(event.memberId, event.adyenMerchantAccount)
    }

    @EventHandler
    fun on(event: PendingAdyenTokenRegistrationCreatedEvent) {
        logger.info("PendingAdyenTokenRegistrationCreatedEvent - Account created/updated [MemberId: ${event.memberId}] [MerchantAccount: ${event.adyenMerchantAccount}]")
        createOrUpdateAdyenAccountWithMerchantInfo(event.memberId, event.adyenMerchantAccount)
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

    private fun createOrUpdateAdyenAccountWithMerchantInfo(memberId: String, merchantAccount: String) {
        val accountMaybe = adyenAccountRepository.findById(memberId)

        if (accountMaybe.isPresent) {
            val account = accountMaybe.get()
            account.merchantAccount = merchantAccount
            adyenAccountRepository.save(account)
        } else {
            adyenAccountRepository.save(
                AdyenAccount(
                    memberId = memberId,
                    merchantAccount = merchantAccount
                )
            )
        }
    }

    private fun updateAndSave(
        memberId: String,
        recurringDetailReference: String,
        accountStatus: AdyenAccountStatus
    ) {
        val adyenAccountMaybe = adyenAccountRepository.findById(memberId)

        if (!adyenAccountMaybe.isPresent) {
            throw IllegalStateException(
                "Cannot find adyen account for member $memberId " +
                    "[Reference: $recurringDetailReference] [Status: $accountStatus]"
            )
        }

        val adyenAccountToUpdate = adyenAccountMaybe.get()

        adyenAccountToUpdate.recurringDetailReference = recurringDetailReference
        adyenAccountToUpdate.accountStatus = accountStatus

        adyenAccountRepository.save(adyenAccountToUpdate)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
