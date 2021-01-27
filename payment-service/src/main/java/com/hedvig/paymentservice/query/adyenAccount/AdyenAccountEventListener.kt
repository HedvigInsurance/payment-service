package com.hedvig.paymentservice.query.adyenAccount

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
    fun on(event: AdyenAccountCreatedEvent) {
        createOrUpdateAndSave(event.memberId, event.recurringDetailReference, event.accountStatus)
    }

    @EventHandler
    fun on(event: AdyenAccountUpdatedEvent) {
        createOrUpdateAndSave(event.memberId, event.recurringDetailReference, event.accountStatus)
    }

    private fun createOrUpdateAndSave(memberId: String, recurringDetailReference: String, accountStatus: AdyenAccountStatus) {
        val adyenAccountMaybe = adyenAccountRepository.findById(memberId)

        val adyenAccount: AdyenAccount = if (adyenAccountMaybe.isPresent) {
            val adyenAccountToUpdate = adyenAccountMaybe.get()
            adyenAccountToUpdate.recurringDetailReference = recurringDetailReference
            adyenAccountToUpdate.accountStatus = accountStatus
            adyenAccountToUpdate
        } else {
            AdyenAccount(memberId, recurringDetailReference, accountStatus)
        }

        adyenAccountRepository.save(adyenAccount)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }
}
