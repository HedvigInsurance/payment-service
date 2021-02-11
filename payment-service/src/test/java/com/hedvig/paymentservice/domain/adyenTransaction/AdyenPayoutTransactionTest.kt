package com.hedvig.paymentservice.domain.adyenTransaction

import com.adyen.model.payout.ConfirmThirdPartyResponse
import com.adyen.model.payout.SubmitResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionPayoutCommand
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class AdyenPayoutTransactionTest {
    lateinit var fixture: AggregateTestFixture<AdyenPayoutTransaction>

    @MockkBean
    lateinit var adyenService: AdyenService

    @Before
    fun setUp() {
        fixture = AggregateTestFixture(AdyenPayoutTransaction::class.java)
        fixture.registerInjectableResource(adyenService)

        val submitResponse = SubmitResponse()
        submitResponse.resultCode = PAYOUT_SUBMiT_RECEIVED
        submitResponse.pspReference = PSP_REFERENCE

        val confirmThirdPartyResponse = ConfirmThirdPartyResponse()
        confirmThirdPartyResponse.pspReference = PSP_REFERENCE
        confirmThirdPartyResponse.response = "response"

        every { adyenService.startPayoutTransaction(any(), any(), any(), any(), any()) } returns submitResponse
        every { adyenService.confirmPayout(any(), any()) } returns confirmThirdPartyResponse
    }

    @Test
    fun `given a payout is initiated then we expect AdyenPayoutTransactionInitiatedEvent, AdyenPayoutTransactionAuthorisedEvent, AdyenPayoutTransactionConfirmedEvent`() {
        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionPayoutCommand(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    email = ""
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenPayoutTransactionInitiatedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK
                ),
                AdyenPayoutTransactionAuthorisedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    payoutReference = PSP_REFERENCE
                ),
                AdyenPayoutTransactionConfirmedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    payoutReference = PSP_REFERENCE,
                    response = "response"
                )
            )
    }

    @Test
    fun `given a payout is initiated and a startPayoutTransaction method throws exception then expect a AdyenPayoutTransactionCanceledEvent`() {
        every { adyenService.startPayoutTransaction(any(), any(), any(), any(), any()) } throws Exception()
        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionPayoutCommand(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    email = ""
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenPayoutTransactionInitiatedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK
                ),
                AdyenPayoutTransactionCanceledEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    reason = "exception"
                )
            )
    }

    @Test
    fun `given a payout is initiated and a confirmPayout method throws exception then expect a AdyenPayoutTransactionCanceledEvent`() {
        every { adyenService.confirmPayout(any(), any()) } throws Exception()
        fixture.givenNoPriorActivity()
            .`when`(
                InitiateAdyenTransactionPayoutCommand(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    email = ""
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                AdyenPayoutTransactionInitiatedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK
                ), AdyenPayoutTransactionAuthorisedEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    payoutReference = PSP_REFERENCE
                ),
                AdyenPayoutTransactionCanceledEvent(
                    transactionId = AdyenTransactionTest.TRANSACTION_ID_ONE,
                    memberId = AdyenTransactionTest.MEMBER_ID_ONE,
                    shopperReference = "REFERANCE",
                    amount = AdyenTransactionTest.ONE_THOUSAND_NOK,
                    reason = "exception"
                )
            )
    }

    companion object {
        const val PSP_REFERENCE = "pspReference"
        const val PAYOUT_SUBMiT_RECEIVED = "[payout-submit-received]"
    }
}
