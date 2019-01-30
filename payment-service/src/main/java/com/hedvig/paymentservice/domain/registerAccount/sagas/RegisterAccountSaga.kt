package com.hedvig.paymentservice.domain.registerAccount.sagas

import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountConfirmationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountNotificationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountResponseCommand
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class RegisterAccountSaga {
  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @StartSaga
  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: RegisterAccountRequestCreatedEvent) {
    commandGateway.sendAndWait<Any>(CreateOrderCommand(e.memberId, e.hedvigOrderId))
  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: SelectAccountResponseReceivedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveRegisterAccountResponseCommand(e.hedvigOrderId))
  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: AccountNotificationReceivedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveRegisterAccountNotificationCommand(e.hedvigOrderId, e.memberId))
  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  fun on(e: TrustlyAccountCreatedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveRegisterAccountConfirmationCommand(e.hedvigOrderId, e.memberId))
  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  fun on(e: TrustlyAccountUpdatedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveRegisterAccountConfirmationCommand(e.hedvigOrderId, e.memberId))
  }

  companion object {
    const val HEDVIG_ORDER_ID: String = "hedvigOrderId"
  }
}
