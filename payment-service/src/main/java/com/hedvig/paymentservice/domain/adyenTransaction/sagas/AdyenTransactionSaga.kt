package com.hedvig.paymentservice.domain.adyenTransaction.sagas

import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@Saga
class AdyenTransactionSaga {
  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  /*
  * When the capture delay is set to immediate and the authorisation is successful, the funds are immediately captured.
  * Because it is immediate, Adyen only send an authorisation notification.
  * In that scenario, we can indeed consider the transaction is successful after the authorisation.
  *
  * If for some reason the capture fails, we will receive a capture_failed notification as described in Adyen's documentation.
  *
  * */
  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: AdyenTransactionAuthorisedEvent) {
    commandGateway.sendAndWait<Void>(
      ChargeCompletedCommand(
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
  fun on(e: AdyenTransactionCanceledEvent) {
    commandGateway.sendAndWait<Void>(
      ChargeFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId
      )
    )
  }

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: CaptureFailureAdyenTransactionReceivedEvent) {
    commandGateway.sendAndWait<Void>(
      ChargeFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId
      )
    )
  }

  companion object {
    const val TRANSACTION_ID: String = "transactionId"
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }


}
