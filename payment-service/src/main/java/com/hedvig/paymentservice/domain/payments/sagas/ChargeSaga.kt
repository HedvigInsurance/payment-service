package com.hedvig.paymentservice.domain.payments.sagas

import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand
import com.hedvig.paymentservice.services.trustly.TrustlyService
import com.hedvig.paymentservice.services.trustly.dto.PaymentRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class ChargeSaga {
  @Autowired
  @Transient
  lateinit var commandGateway: CommandGateway
  @Autowired
  @Transient
  lateinit var trustlyService: TrustlyService
  @Autowired
  @Transient
  lateinit var uuidGenerator: UUIDGenerator

  @StartSaga
  @SagaEventHandler(associationProperty = "memberId")
  @EndSaga
  fun on(e: ChargeCreatedEvent) {
    when (e.provider) {
      PayinProvider.TRUSTLY -> {
        val hedvigOrderId = uuidGenerator.generateRandom()
        commandGateway.sendAndWait<Any>(
          CreatePaymentOrderCommand(
            hedvigOrderId,
            e.transactionId,
            e.memberId,
            e.amount,
            e.providerId
          )
        )
        trustlyService.startPaymentOrder(
          PaymentRequest(e.memberId, e.amount, e.providerId, e.email),
          hedvigOrderId
        )
      }
      PayinProvider.ADYEN -> {
        commandGateway.sendAndWait<Void>(
          InitiateAdyenTransactionCommand(
            e.transactionId,
            e.memberId,
            e.providerId,
            e.amount
          )
        )
      }
    }
  }
}
