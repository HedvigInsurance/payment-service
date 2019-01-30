package com.hedvig.paymentservice.domain.registerAccount

import com.hedvig.paymentservice.domain.registerAccount.commands.CreateRegisterAccountRequestCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountConfirmationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountNotificationCommand
import com.hedvig.paymentservice.domain.registerAccount.commands.ReceiveRegisterAccountResponseCommand
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountConfirmationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountNotificationReceivedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountRequestCreatedEvent
import com.hedvig.paymentservice.domain.registerAccount.events.RegisterAccountResponseReceivedEvent
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Before
import org.junit.Test
import java.util.*

class RegisterAccountTest {
  lateinit var fixture: AggregateTestFixture<RegisterAccount>

  @Before
  fun setUp() {
    fixture = AggregateTestFixture(RegisterAccount::class.java)
  }

  @Test
  fun given_noPriorActivity_when_CreateRegisterAccountRequestCommandIsExecuted_expect_RegisterAccountRequestCreatedEvent() {
    fixture.givenNoPriorActivity()
      .`when`(CreateRegisterAccountRequestCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
  }

  @Test
  fun given_RegisterAccountRequestCreatedEvent_when_ReceiveRegisterAccountResponseCommandIsExecuted_expect_RegisterAccountResponseReceivedEvent() {
    fixture.given(RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .`when`(ReceiveRegisterAccountResponseCommand(TEST_HEDVIG_ORDER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(RegisterAccountResponseReceivedEvent(TEST_HEDVIG_ORDER_ID))
  }

  @Test
  fun given_lastEventOfTheAggregateIsRegisterAccountResponseReceivedEvent_when_ReceiveRegisterAccountNotificationCommandIsExecuted_expect_RegisterAccountNotificationReceivedEvent() {
    fixture.given(
      RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID), RegisterAccountResponseReceivedEvent(
        TEST_HEDVIG_ORDER_ID
      )
    )
      .`when`(ReceiveRegisterAccountNotificationCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(RegisterAccountNotificationReceivedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
  }

  @Test
  fun given_lastEventOfTheAggregateIsRegisterAccountNotificationReceivedEvent_when_ReceiveRegisterAccountConfirmationCommandIsExecuted_expect_RegisterAccountConfirmationReceivedEvent() {
    fixture.given(
      RegisterAccountRequestCreatedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID),
      RegisterAccountResponseReceivedEvent(TEST_HEDVIG_ORDER_ID),
      RegisterAccountNotificationReceivedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID)
    )
      .`when`(ReceiveRegisterAccountConfirmationCommand(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
      .expectSuccessfulHandlerExecution()
      .expectEvents(RegisterAccountConfirmationReceivedEvent(TEST_HEDVIG_ORDER_ID, TEST_MEMBER_ID))
  }

  companion object {
    val TEST_HEDVIG_ORDER_ID: UUID = UUID.fromString("dbe1a7dc-2490-11e9-a718-dbca6dd113e4")
    const val TEST_MEMBER_ID: String = "RemarkableMemberId1"
  }

}
