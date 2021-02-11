package com.hedvig.paymentservice.domain.adyenTransaction.sagas

import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.DeclinedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ExpiredAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.FailedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ReservedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.SuccessfulAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand
import java.time.Instant
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AdyenPayoutTransactionSaga {
  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: SuccessfulAdyenPayoutTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutCompletedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: AdyenPayoutTransactionCanceledEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: FailedAdyenPayoutTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: DeclinedAdyenPayoutTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: ExpiredAdyenPayoutTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: ReservedAdyenPayoutTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      PayoutFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }

  companion object {
    const val TRANSACTION_ID: String = "transactionId"
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }
}
