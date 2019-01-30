package com.hedvig.paymentservice.domain.registerAccount.saga

import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountConfirmationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountNotificationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountResponseCommand
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.registerAccount.sagas.RegisterAccountSaga
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent
import org.axonframework.test.saga.SagaTestFixture
import org.junit.Before
import org.junit.Test
import java.util.*

class RegisterAccountSagaTest {

  lateinit var fixture: SagaTestFixture<RegisterAccountSaga>

  @Before
  fun setUp() {
    fixture = SagaTestFixture(RegisterAccountSaga::class.java)
  }

  @Test
  fun given_noPriorActivity_when_RegisterAccountRequestCreatedEventArrives_expect_CreateOrderCommandToBeDispatched() {
    fixture
      .givenNoPriorActivity()
      .whenPublishingA(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectActiveSagas(1)
      .expectDispatchedCommands(CreateOrderCommand(TEST_MEMBER_ID, TEST_HEDVIG_ORDER_ID))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_SelectAccountResponseReceivedEventArrives_expect_ReceiveRegisterAccountResponseCommandToBeDispatched() {
    fixture
      .givenAPublished(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .whenPublishingA(SelectAccountResponseReceivedEvent(TEST_HEDVIG_ORDER_ID, ""))
      .expectActiveSagas(1)
      .expectDispatchedCommands(ReceiveRegisterAccountResponseCommand(TEST_HEDVIG_ORDER_ID))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_AccountNotificationReceivedEventArrives_expect_ReceiveRegisterAccountResponseCommandToBeDispatched() {
    fixture
      .givenAPublished(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .whenPublishingA(
        AccountNotificationReceivedEvent(
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          null,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING
        )
      )
      .expectActiveSagas(1)
      .expectDispatchedCommands(ReceiveRegisterAccountNotificationCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_TrustlyAccountCreatedEventArrives_expect_ReceiveRegisterAccountConfirmationCommandToBeDispatched() {
    fixture
      .givenAPublished(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .whenPublishingA(
        TrustlyAccountCreatedEvent(
          TEST_MEMBER_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING
        )
      )
      .expectDispatchedCommands(ReceiveRegisterAccountConfirmationCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectActiveSagas(0)
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_TrustlyAccountUpdatedEventArrives_expect_ReceiveRegisterAccountConfirmationCommandToBeDispatched() {
    fixture
      .givenAPublished(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .whenPublishingA(
        TrustlyAccountUpdatedEvent(
          TEST_MEMBER_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING,
          TEST_STRING
        )
      )
      .expectDispatchedCommands(ReceiveRegisterAccountConfirmationCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectActiveSagas(0)
  }


  companion object {
    val TEST_HEDVIG_ORDER_ID: UUID = UUID.fromString("dbe1a7dc-2490-11e9-a718-dbca6dd113e4")
    const val TEST_MEMBER_ID: String = "RemarkableMemberId1"
    const val TEST_STRING: String = "RemarkableTestString"
  }

}
