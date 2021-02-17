package com.hedvig.paymentservice.domain.swish

import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand
import com.hedvig.paymentservice.domain.swish.commands.InitiateSwishTransactionPayoutCommand
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionCompletedCommand
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionCompletedEvent
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionFailedCommand
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionFailedEvent
import com.hedvig.paymentservice.domain.swish.events.SwishPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.services.swish.StartPayoutResponse
import com.hedvig.paymentservice.services.swish.SwishService
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import javax.money.MonetaryAmount

@Aggregate
class SwishPayoutTransaction() {
    @AggregateIdentifier
    lateinit var transactionId: UUID

    private lateinit var memberId: String
    private lateinit var transactionStatus: SwishPayoutTransactionStatus
    private lateinit var amount: MonetaryAmount

    @Autowired
    @Transient
    private lateinit var commandGateway: CommandGateway

    @CommandHandler
    constructor(
        cmd: InitiateSwishTransactionPayoutCommand,
        swishService: SwishService
    ) : this() {
        AggregateLifecycle.apply(
            SwishPayoutTransactionInitiatedEvent(
                transactionId = cmd.transactionId,
                memberId = cmd.memberId,
                phoneNumber = cmd.phoneNumber,
                ssn = cmd.ssn,
                message = cmd.message,
                amount = cmd.amount
            )
        )

        val response = swishService.startPayout(
            transactionId = cmd.transactionId,
            memberId = cmd.memberId,
            payeeAlias = cmd.phoneNumber,
            payeeSSN = cmd.ssn,
            amount = cmd.amount,
            message = cmd.message,
            instructionDate = LocalDateTime.now()
        )

        when (response) {
            StartPayoutResponse.Success -> AggregateLifecycle.apply(
                SwishPayoutTransactionConfirmedEvent(
                    transactionId = cmd.transactionId,
                    memberId = cmd.memberId
                )
            )
            is StartPayoutResponse.Failed -> AggregateLifecycle.apply(
                SwishPayoutTransactionCanceledEvent(
                    transactionId = cmd.transactionId,
                    memberId = cmd.memberId,
                    reason = response.exceptionMessage ?: "exception",
                    httpStatusCode = response.status
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: AdyenPayoutTransactionInitiatedEvent) {
        transactionId = e.transactionId
        memberId = e.memberId
        transactionStatus = SwishPayoutTransactionStatus.INITIATED
        amount = e.amount
    }

    @EventSourcingHandler
    fun on(e: SwishPayoutTransactionCanceledEvent) {
        transactionStatus = SwishPayoutTransactionStatus.CANCELLED
        commandGateway.sendAndWait<Any>(
            PayoutFailedCommand(
                memberId = e.memberId,
                transactionId = e.transactionId,
                amount = amount,
                timestamp = Instant.now()
            )
        )
    }

    @EventSourcingHandler
    fun on(e: SwishPayoutTransactionConfirmedEvent) {
        transactionStatus = SwishPayoutTransactionStatus.CONFIRMED
    }

    @EventSourcingHandler
    fun on(e: SwishPayoutTransactionCompletedEvent) {
        transactionStatus = SwishPayoutTransactionStatus.COMPLETED
        commandGateway.sendAndWait<Any>(
            PayoutCompletedCommand(
                memberId = e.memberId,
                transactionId = e.transactionId,
                amount = amount,
                timestamp = Instant.now()
            )
        )
    }

    @EventSourcingHandler
    fun on(e: SwishPayoutTransactionFailedEvent) {
        transactionStatus = SwishPayoutTransactionStatus.FAILED
        commandGateway.sendAndWait<Any>(
            PayoutFailedCommand(
                memberId = e.memberId,
                transactionId = e.transactionId,
                amount = amount,
                timestamp = Instant.now()
            )
        )
    }

    @CommandHandler
    fun handle(cmd: SwishPayoutTransactionCompletedCommand) {
        if (transactionStatus != SwishPayoutTransactionStatus.COMPLETED) {
            AggregateLifecycle.apply(
                SwishPayoutTransactionCompletedEvent(
                    transactionId = cmd.transactionId,
                    memberId = cmd.memberId
                )
            )
        }
    }

    @CommandHandler
    fun handle(cmd: SwishPayoutTransactionFailedCommand) {
        if (transactionStatus != SwishPayoutTransactionStatus.FAILED) {
            AggregateLifecycle.apply(
                SwishPayoutTransactionFailedEvent(
                    transactionId = cmd.transactionId,
                    memberId = cmd.memberId,
                    errorCode = cmd.errorCode,
                    errorMessage = cmd.errorMessage,
                    additionalInformation = cmd.additionalInformation
                )
            )
        }
    }
}
