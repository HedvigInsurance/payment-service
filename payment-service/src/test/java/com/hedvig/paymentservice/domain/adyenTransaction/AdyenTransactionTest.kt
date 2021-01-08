package com.hedvig.paymentservice.domain.adyenTransaction

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveAuthorisationAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCancellationResponseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceiveCaptureFailureAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionAutoRescueProcessStartedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionPendingResponseReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.CaptureFailureAdyenTransactionReceivedEvent
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.axonframework.test.aggregate.AggregateTestFixture
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID

@RunWith(SpringRunner::class)
class AdyenTransactionTest {
    lateinit var fixture: AggregateTestFixture<AdyenTransaction>

    @MockkBean
    lateinit var adyenService: AdyenService

    @Before
    fun setUp() {
        fixture = AggregateTestFixture(AdyenTransaction::class.java)
        fixture.registerInjectableResource(adyenService)

        every { adyenService.chargeMemberWithToken(any()) } returns PaymentsResponse()
    }

    @Test
    fun given_noPriorActivity_when_InitiateAdyenTransactionCommand_and_AdyenResponseIsAuthorised_expect_AdyenTransactionInitiatedEvent_And_AdyenTransactionAuthorisedEvent() {
        val response = PaymentsResponse()
        response.resultCode = PaymentsResponse.ResultCodeEnum.AUTHORISED
        every { adyenService.chargeMemberWithToken(any()) } returns response

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenTransactionInitiatedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                ),
                AdyenTransactionAuthorisedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
    }

    @Test
    fun given_noPriorActivity_when_InitiateAdyenTransactionCommand_and_AdyenResponseIsPending_expect_AdyenTransactionInitiatedEvent_And_AdyenTransactionAuthorisedEvent() {
        val response = PaymentsResponse()
        response.resultCode = PaymentsResponse.ResultCodeEnum.PENDING
        every { adyenService.chargeMemberWithToken(any()) } returns response

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenTransactionInitiatedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                ),
                AdyenTransactionPendingResponseReceivedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    reason = PaymentsResponse.ResultCodeEnum.PENDING.value
                )
            )
    }

    @Test
    fun given_noPriorActivity_when_InitiateAdyenTransactionCommand_and_AdyenResponseIsRefused_expect_AdyenTransactionInitiatedEvent_And_AdyenTransactionCanceledEvent() {
        val response = PaymentsResponse()
        response.resultCode = PaymentsResponse.ResultCodeEnum.REFUSED
        every { adyenService.chargeMemberWithToken(any()) } returns response

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenTransactionInitiatedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                ),
                AdyenTransactionCanceledEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK,
                    reason = PaymentsResponse.ResultCodeEnum.REFUSED.value
                )
            )
    }

    @Test
    fun when_InitiateAdyenTransactionCommand_and_AdyenResponseIsRefusedWithAutoRescue_expect_AdyenTransactionInitiatedEvent_And_AdyenTransactionAutoRescueProcessStartedEvent() {
        val response = PaymentsResponse()
        response.resultCode = PaymentsResponse.ResultCodeEnum.REFUSED
        response.refusalReason = "NOT_ENOUGH_BALANCE"
        response.additionalData = mapOf("retry.rescueScheduled" to "true", "retry.rescueReference" to "LOLOLOLOLOL")
        every { adyenService.chargeMemberWithToken(any()) } returns response

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenTransactionInitiatedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                ),
                AdyenTransactionAutoRescueProcessStartedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    amount = ONE_THOUSAND_NOK,
                    reason = response.refusalReason,
                    rescueReference = response.additionalData["retry.rescueReference"]!!
                )
            )
    }

    @Test
    fun given_noPriorActivity_when_InitiateAdyenTransactionCommand_and_AdyenThrowsException_expect_AdyenTransactionInitiatedEvent_And_AdyenTransactionAuthorisedEvent() {
        every { adyenService.chargeMemberWithToken(any()) } throws RuntimeException("LALALA")

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenTransactionInitiatedEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK
                ),
                AdyenTransactionCanceledEvent(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                    amount = ONE_THOUSAND_NOK,
                    reason = "LALALA"
                )
            )
    }

    @Test
    fun given_AdyenTransactionCanceledEvent_when_ReceiveCancellationResponseAdyenTransactionCommand_expect_NoEvent() {
        fixture.given(
            AdyenTransactionInitiatedEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE,
                recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                amount = ONE_THOUSAND_NOK
            ),
            AdyenTransactionCanceledEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE,
                recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                amount = ONE_THOUSAND_NOK,
                reason = "LALALA"
            )
        )
            .`when`(
                ReceiveCancellationResponseAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    reason = "Random reason"
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
    }

    @Test
    fun given_AdyenTransactionAuthorisedEvent_when_ReceiveAuthorisationAdyenTransactionCommand_expect_NoEvent() {
        fixture.given(
            AdyenTransactionInitiatedEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE,
                recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                amount = ONE_THOUSAND_NOK
            ),
            AdyenTransactionAuthorisedEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE,
                recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                amount = ONE_THOUSAND_NOK
            )
        )
            .`when`(
                ReceiveAuthorisationAdyenTransactionCommand(
                    transactionId = TRANSACTION_ID_ONE,
                    memberId = MEMBER_ID_ONE,
                    amount = ONE_THOUSAND_NOK,
                    rescueReference = null
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
    }

    @Test
    fun given_CaptureFailureAdyenTransactionReceivedEvent_when_ReceiveCaptureFailureAdyenTransactionCommand_expect_NoEvent() {
        fixture.given(
            AdyenTransactionInitiatedEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE,
                recurringDetailReference = RECURRING_REFERENCE_ID_ONE,
                amount = ONE_THOUSAND_NOK
            ),
            CaptureFailureAdyenTransactionReceivedEvent(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE
            )
        ).`when`(
            ReceiveCaptureFailureAdyenTransactionCommand(
                transactionId = TRANSACTION_ID_ONE,
                memberId = MEMBER_ID_ONE
            )
        ).expectSuccessfulHandlerExecution()
            .expectNoEvents()
    }

    companion object {
        val TRANSACTION_ID_ONE = UUID.fromString("CA691F10-E50C-4D77-AC9A-53213C377BFE")
        const val MEMBER_ID_ONE = "MEMBER_ONE"
        const val RECURRING_REFERENCE_ID_ONE = "RECURRING_REFERENCE_ID_ONE"
        val ONE_THOUSAND_NOK = Money.of(1000, "NOK")
    }
}
