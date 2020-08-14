package com.hedvig.paymentservice.query.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.SuccessfulAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.DeclinedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ExpiredAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.FailedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ReservedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.query.adyenTransaction.entities.AdyenPayoutTransactionRepository
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AdyenPayoutTransactionEventListener(
   val adyenPayoutTransactionRepository: AdyenPayoutTransactionRepository
) {
  @EventHandler
  fun on(e: AdyenPayoutTransactionInitiatedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionAuthorisedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionConfirmedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: AdyenPayoutTransactionCanceledEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: SuccessfulAdyenPayoutTransactionReceivedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: FailedAdyenPayoutTransactionReceivedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: DeclinedAdyenPayoutTransactionReceivedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: ExpiredAdyenPayoutTransactionReceivedEvent) {
    TODO()
  }

  @EventHandler
  fun on(e: ReservedAdyenPayoutTransactionReceivedEvent) {
    TODO()
  }
}
