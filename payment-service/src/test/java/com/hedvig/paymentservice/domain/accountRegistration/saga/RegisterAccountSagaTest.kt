package com.hedvig.paymentservice.domain.accountRegistration.saga

import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationConfirmationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationNotificationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationResponseCommand
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationRequestCreatedEvent
import com.hedvig.paymentservice.domain.accountRegistration.sagas.AccountRegistrationSaga
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent
import org.axonframework.test.saga.SagaTestFixture
import org.junit.Before
import org.junit.Test
import java.util.*

class RegisterAccountSagaTest {

  lateinit var fixture: SagaTestFixture<AccountRegistrationSaga>

  @Before
  fun setUp() {
    fixture = SagaTestFixture(AccountRegistrationSaga::class.java)
  }

  @Test
  fun given_noPriorActivity_when_RegisterAccountRequestCreatedEventArrives_expect_CreateOrderCommandToBeDispatched() {
    fixture
      .givenNoPriorActivity()
      .whenPublishingA(
        AccountRegistrationRequestCreatedEvent(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_TRUSTLY_ORDER_ID,
          TEST_TRUSTLY_URL
        )
      )
      .expectActiveSagas(1)
      .expectDispatchedCommands(
        CreateOrderCommand(TEST_MEMBER_ID, TEST_HEDVIG_ORDER_ID), SelectAccountResponseReceivedCommand(
          TEST_HEDVIG_ORDER_ID, TEST_TRUSTLY_URL, TEST_TRUSTLY_ORDER_ID
        )
      )
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_SelectAccountResponseReceivedEventArrives_expect_ReceiveRegisterAccountResponseCommandToBeDispatched() {
    fixture
      .givenAPublished(
        AccountRegistrationRequestCreatedEvent(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_TRUSTLY_ORDER_ID,
          TEST_TRUSTLY_URL
        )
      )
      .whenPublishingA(SelectAccountResponseReceivedEvent(TEST_HEDVIG_ORDER_ID, TEST_TRUSTLY_URL))
      .expectActiveSagas(1)
      .expectDispatchedCommands(ReceiveAccountRegistrationResponseCommand(TEST_ACCOUNT_REGISTRATION_ID))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_AccountNotificationReceivedEventArrives_expect_ReceiveRegisterAccountResponseCommandToBeDispatched() {
    fixture
      .givenAPublished(
        AccountRegistrationRequestCreatedEvent(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_TRUSTLY_ORDER_ID,
          TEST_TRUSTLY_URL
        )
      )
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
      .expectDispatchedCommands(
        ReceiveAccountRegistrationNotificationCommand(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_MEMBER_ID
        )
      )
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_TrustlyAccountCreatedEventArrives_expect_ReceiveRegisterAccountConfirmationCommandToBeDispatched() {
    fixture
      .givenAPublished(
        AccountRegistrationRequestCreatedEvent(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_TRUSTLY_ORDER_ID,
          TEST_TRUSTLY_URL
        )
      )
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
      .expectDispatchedCommands(
        ReceiveAccountRegistrationConfirmationCommand(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_MEMBER_ID
        )
      )
      .expectActiveSagas(0)
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_TrustlyAccountUpdatedEventArrives_expect_ReceiveRegisterAccountConfirmationCommandToBeDispatched() {
    fixture
      .givenAPublished(
        AccountRegistrationRequestCreatedEvent(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_HEDVIG_ORDER_ID,
          TEST_MEMBER_ID,
          TEST_TRUSTLY_ORDER_ID,
          TEST_TRUSTLY_URL
        )
      )
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
      .expectDispatchedCommands(
        ReceiveAccountRegistrationConfirmationCommand(
          TEST_ACCOUNT_REGISTRATION_ID,
          TEST_MEMBER_ID
        )
      )
      .expectActiveSagas(0)
  }


  companion object {
    val TEST_ACCOUNT_REGISTRATION_ID: UUID = UUID.fromString("dbe1a7dc-2490-11e9-a718-dbca6dd113e4")
    val TEST_HEDVIG_ORDER_ID: UUID = UUID.fromString("286f8170-2868-11e9-b424-4b92cc4a6e6b")
    const val TEST_TRUSTLY_ORDER_ID: String = "RemarkableTrustlyOrderId"
    const val TEST_TRUSTLY_URL: String = "RemarkableTrustlyUrl"
    const val TEST_MEMBER_ID: String = "RemarkableMemberId1"
    const val TEST_STRING: String = "RemarkableTestString"
  }

}
