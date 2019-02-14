package com.hedvig.paymentservice.domain.accountRegistration.sagas

import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationConfirmationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationNotificationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationResponseCommand
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationCancellationReceivedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationRequestCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.SagaLifecycle
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Saga
class AccountRegistrationSaga {

  private val log: Logger = LoggerFactory.getLogger(AccountRegistrationSaga::class.java)

  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  private lateinit var accountRegistrationId: UUID

  @StartSaga
  @SagaEventHandler(associationProperty = ACCOUNT_REGISTRATION_ID)
  fun on(e: AccountRegistrationRequestCreatedEvent) {
    accountRegistrationId = e.accountRegistrationId

    SagaLifecycle.associateWith(HEDVIG_ORDER_ID, e.hedvigOrderId.toString())

    commandGateway.sendAndWait<Any>(CreateOrderCommand(e.memberId, e.hedvigOrderId))
    commandGateway.sendAndWait<Any>(
      SelectAccountResponseReceivedCommand(
        e.hedvigOrderId,
        e.trustlyUrl,
        e.trustlyOrderId
      )
    )
  }

  @SagaEventHandler(associationProperty = ACCOUNT_REGISTRATION_ID)
  @EndSaga
  fun on(e: AccountRegistrationCancellationReceivedEvent) {
  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: SelectAccountResponseReceivedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveAccountRegistrationResponseCommand(accountRegistrationId))

  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  fun on(e: AccountNotificationReceivedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveAccountRegistrationNotificationCommand(accountRegistrationId, e.memberId))

  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  fun on(e: TrustlyAccountCreatedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveAccountRegistrationConfirmationCommand(accountRegistrationId, e.memberId))

  }

  @SagaEventHandler(associationProperty = HEDVIG_ORDER_ID)
  @EndSaga
  fun on(e: TrustlyAccountUpdatedEvent) {
    commandGateway.sendAndWait<Any>(ReceiveAccountRegistrationConfirmationCommand(accountRegistrationId, e.memberId))

  }

  companion object {
    const val HEDVIG_ORDER_ID: String = "hedvigOrderId"
    const val ACCOUNT_REGISTRATION_ID: String = "accountRegistrationId"
  }
}
