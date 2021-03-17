package com.hedvig.paymentservice.domain.swish

import com.hedvig.paymentservice.domain.swish.commands.InitiateSwishTransactionPayoutCommand
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionCompletedCommand
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionFailedCommand
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.services.swish.dto.StartPayoutResponse
import com.hedvig.paymentservice.services.swish.SwishService
import io.mockk.every
import io.mockk.mockk
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.AbstractAggregateFactory
import org.axonframework.eventsourcing.DomainEventMessage
import org.axonframework.test.aggregate.AggregateTestFixture
import org.javamoney.moneta.Money
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class SwishPayoutTransactionTest {

    lateinit var fixture: AggregateTestFixture<SwishPayoutTransaction>

    val swishService: SwishService = mockk()
    val commandGateway: CommandGateway = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(SwishPayoutTransaction::class.java)
        fixture.registerAggregateFactory(AggregateFactory(SwishPayoutTransaction::class.java))
        fixture.registerInjectableResource(swishService)
    }

    @Test
    fun `on InitiateSwishTransactionPayoutCommand with successful start of payout expect SwishPayoutTransactionInitiatedEvent and SwishPayoutTransactionConfirmedEvent`() {
        every { swishService.startPayout(any(), any(), any(), any(), any(), any(), any()) } returns StartPayoutResponse.Success

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateSwishTransactionPayoutCommand(
                    transactionId,
                    memberId,
                    phoneNumber,
                    ssn,
                    message,
                    amount
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SwishPayoutTransactionInitiatedEvent(
                    transactionId,
                    memberId,
                    phoneNumber,
                    ssn,
                    message,
                    amount
                ),
                SwishPayoutTransactionConfirmedEvent(
                    transactionId,
                    memberId
                )
            )
    }

    /* FIXME: this test fails with the message:
       Caused by: kotlin.UninitializedPropertyAccessException: lateinit property commandGateway has not been initialized
       the strange part is that the test above dose not.
       If you figure out why I will give you a cookie!
    @Test
    fun `on InitiateSwishTransactionPayoutCommand with failed start of payout expect SwishPayoutTransactionInitiatedEvent and SwishPayoutTransactionCanceledEvent`() {
        every { swishService.startPayout(any(), any(), any(), any(), any(), any(), any()) } returns StartPayoutResponse.Failed("message", 422)

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateSwishTransactionPayoutCommand(
                    transactionId,
                    memberId,
                    phoneNumber,
                    ssn,
                    message,
                    amount
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SwishPayoutTransactionInitiatedEvent(
                    transactionId,
                    memberId,
                    phoneNumber,
                    ssn,
                    message,
                    amount
                ),
                SwishPayoutTransactionCanceledEvent(
                    transactionId,
                    memberId,
                    "message",
                    422
                )
            )
    }
    */

    @Test
    fun `on InitiateSwishTransactionPayoutCommand with amount in NOK should create no events`() {
        every { swishService.startPayout(any(), any(), any(), any(), any(), any(), any()) } returns StartPayoutResponse.Success

        fixture.givenNoPriorActivity()
            .`when`(
                InitiateSwishTransactionPayoutCommand(
                    transactionId,
                    memberId,
                    phoneNumber,
                    ssn,
                    message,
                    Money.of(10, "NOK")
                )
            )
            .expectException(IllegalArgumentException::class.java)
            .expectNoEvents()
    }

    @Test
    fun `on SwishPayoutTransactionCompletedCommand when already completed create no events`() {
        every { swishService.startPayout(any(), any(), any(), any(), any(), any(), any()) } returns StartPayoutResponse.Success

        fixture.givenCommands(
            InitiateSwishTransactionPayoutCommand(
                transactionId,
                memberId,
                phoneNumber,
                ssn,
                message,
                amount
            ),
            SwishPayoutTransactionCompletedCommand(
                transactionId,
                memberId
            )
        )
            .`when`(
                SwishPayoutTransactionCompletedCommand(
                    transactionId,
                    memberId
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
    }

    @Test
    fun `on SwishPayoutTransactionFailedCommand when already failed create no events`() {
        every { swishService.startPayout(any(), any(), any(), any(), any(), any(), any()) } returns StartPayoutResponse.Success

        fixture.givenCommands(
            InitiateSwishTransactionPayoutCommand(
                transactionId,
                memberId,
                phoneNumber,
                ssn,
                message,
                amount
            ),
            SwishPayoutTransactionFailedCommand(
                transactionId,
                memberId,
                "",
                "",
                ""
            )
        )
            .`when`(
                SwishPayoutTransactionFailedCommand(
                    transactionId,
                    memberId,
                    "",
                    "",
                    ""
                )
            )
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
    }

    private inner class AggregateFactory<T> internal constructor(aggregateType: Class<T>?) : AbstractAggregateFactory<T>(aggregateType) {
        override fun doCreateAggregate(aggregateIdentifier: String, firstEvent: DomainEventMessage<*>?): T {
            val swishPayoutTransaction = SwishPayoutTransaction()
            swishPayoutTransaction.commandGateway = commandGateway
            return swishPayoutTransaction as T
        }
    }

    companion object {

        val transactionId = UUID.randomUUID()
        val memberId = "1234"
        val phoneNumber = "phonenumber"
        val ssn = "ssn"
        val message = "message"
        val amount = Money.of(10, "SEK")

    }
}
