package com.hedvig.paymentservice.domain.registerAccount.sagas

import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class SelectAccountSaga(
  @Autowired
  @Transient
  val commandGateway: CommandGateway
) {

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: RegisterAccountRequestCreatedEvent) {
    commandGateway.sendAndWait<Any>(CreateOrderCommand(e.memberId, e.hedvigOrderId))
  }

  companion object {
    const val HEDVIG_ORDER_ID: String = "hedvigOrderId"
  }
}
