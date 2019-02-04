package com.hedvig.paymentservice.domain.accountRegistration

import com.hedvig.paymentservice.domain.accountRegistration.commands.CreateAccountRegistrationRequestCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationConfirmationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationNotificationCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationResponseCommand
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationConfirmationReceivedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationNotificationReceivedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationRequestCreatedEvent
import com.hedvig.paymentservice.domain.accountRegistration.events.AccountRegistrationResponseReceivedEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Before
import org.junit.Test
import java.util.*

class RegisterAccountTest {
  lateinit var fixture: AggregateTestFixture<AccountRegistrationAggregate>

  @Before
  fun setUp() {
    fixture = AggregateTestFixture(AccountRegistrationAggregate::class.java)
  }

  @Test
  fun given_noPriorActivity_when_CreateRegisterAccountRequestCommandIsExecuted_expect_RegisterAccountRequestCreatedEvent() {
    fixture.givenNoPriorActivity()
      .`when`(CreateAccountRegistrationRequestCommand(TEST_ACCOUNT_REGISTRATION_ID, TEST_HEDVIG_ID, TEST_MEMBER_ID, TEST_TRUSTLY_ORDER_ID, TEST_TRUSTLY_URL))
      .expectSuccessfulHandlerExecution()
      .expectEvents(AccountRegistrationRequestCreatedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_HEDVIG_ID, TEST_MEMBER_ID, TEST_TRUSTLY_ORDER_ID, TEST_TRUSTLY_URL))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_ReceiveRegisterAccountResponseCommandIsExecuted_expect_RegisterAccountResponseReceivedEvent() {
    fixture.given(AccountRegistrationRequestCreatedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_HEDVIG_ID, TEST_MEMBER_ID, TEST_TRUSTLY_ORDER_ID, TEST_TRUSTLY_URL))
      .`when`(ReceiveAccountRegistrationResponseCommand(TEST_ACCOUNT_REGISTRATION_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(AccountRegistrationResponseReceivedEvent(TEST_ACCOUNT_REGISTRATION_ID))
  }

  @Test
  fun given_lastEventOfTheAggregateIsRegisterAccountResponseReceivedEvent_when_ReceiveRegisterAccountNotificationCommandIsExecuted_expect_RegisterAccountNotificationReceivedEvent() {
    fixture.given(
      AccountRegistrationRequestCreatedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_HEDVIG_ID, TEST_MEMBER_ID, TEST_TRUSTLY_ORDER_ID, TEST_TRUSTLY_URL), AccountRegistrationResponseReceivedEvent(
        TEST_ACCOUNT_REGISTRATION_ID
      )
    )
      .`when`(ReceiveAccountRegistrationNotificationCommand(TEST_ACCOUNT_REGISTRATION_ID, TEST_MEMBER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(AccountRegistrationNotificationReceivedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_MEMBER_ID))
  }

  @Test
  fun given_lastEventOfTheAggregateIsRegisterAccountNotificationReceivedEvent_when_ReceiveRegisterAccountConfirmationCommandIsExecuted_expect_RegisterAccountConfirmationReceivedEvent() {
    fixture.given(
      AccountRegistrationRequestCreatedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_HEDVIG_ID, TEST_MEMBER_ID, TEST_TRUSTLY_ORDER_ID, TEST_TRUSTLY_URL),
      AccountRegistrationResponseReceivedEvent(TEST_ACCOUNT_REGISTRATION_ID),
      AccountRegistrationNotificationReceivedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_MEMBER_ID)
    )
      .`when`(ReceiveAccountRegistrationConfirmationCommand(TEST_ACCOUNT_REGISTRATION_ID, TEST_MEMBER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(AccountRegistrationConfirmationReceivedEvent(TEST_ACCOUNT_REGISTRATION_ID, TEST_MEMBER_ID))
  }

  companion object {
    val TEST_ACCOUNT_REGISTRATION_ID: UUID = UUID.fromString("dbe1a7dc-2490-11e9-a718-dbca6dd113e4")
    const val TEST_MEMBER_ID: String = "RemarkableMemberId1"
    val TEST_HEDVIG_ID: UUID = UUID.fromString("286f8170-2868-11e9-b424-4b92cc4a6e6b")
    const val TEST_TRUSTLY_ORDER_ID: String = "RemarkableTrustlyOrderId"
    const val TEST_TRUSTLY_URL: String = "RemarkableTrustlyUrl"
  }

}
