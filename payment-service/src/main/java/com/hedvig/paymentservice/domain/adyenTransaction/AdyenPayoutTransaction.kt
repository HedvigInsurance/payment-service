package com.hedvig.paymentservice.domain.adyenTransaction

import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionPayoutCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedFailedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedReservedAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand
import com.hedvig.paymentservice.domain.adyenTransaction.enums.AdyenPayoutTransactionStatus
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionAuthorisedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionConfirmedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenPayoutTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.DeclinedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ExpiredAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.FailedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.ReservedAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.SuccessfulAdyenPayoutTransactionReceivedEvent
import com.hedvig.paymentservice.services.adyen.AdyenService
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import java.util.UUID

@Aggregate
class AdyenPayoutTransaction() {
    @AggregateIdentifier
    lateinit var transactionId: UUID
    lateinit var memberId: String
    lateinit var shopperReference: String
    lateinit var transactionStatus: AdyenPayoutTransactionStatus

    @CommandHandler
    constructor(
        cmd: InitiateAdyenTransactionPayoutCommand,
        adyenService: AdyenService
    ) : this() {
        apply(
            AdyenPayoutTransactionInitiatedEvent(
                cmd.transactionId,
                cmd.memberId,
                cmd.shopperReference,
                cmd.amount
            )
        )
        try {
            val response = adyenService.startPayoutTransaction(
                memberId = cmd.memberId,
                payoutReference = cmd.transactionId.toString(),
                amount = cmd.amount,
                shopperReference = cmd.shopperReference,
                shopperEmail = cmd.email
            )

            when (response.resultCode) {
                "[payout-submit-received]" -> {
                    apply(
                        AdyenPayoutTransactionAuthorisedEvent(
                            cmd.transactionId,
                            cmd.memberId,
                            cmd.shopperReference,
                            cmd.amount,
                            response.pspReference
                        )
                    )

                    val confirmationResponse = adyenService.confirmPayout(
                        payoutReference = response.pspReference,
                        memberId = cmd.memberId
                    )

                    apply(
                        AdyenPayoutTransactionConfirmedEvent(
                            cmd.transactionId,
                            cmd.memberId,
                            cmd.shopperReference,
                            cmd.amount,
                            confirmationResponse.pspReference,
                            confirmationResponse.response
                        )
                    )
                }
                else ->
                    apply(
                        AdyenPayoutTransactionCanceledEvent(
                            cmd.transactionId,
                            cmd.memberId,
                            cmd.shopperReference,
                            cmd.amount,
                            response.resultCode
                        )
                    )
            }
        } catch (ex: Exception) {
            apply(
                AdyenPayoutTransactionCanceledEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.shopperReference,
                    cmd.amount,
                    ex.message ?: "exception"
                )
            )
        }
    }


    @EventSourcingHandler
    fun on(e: AdyenPayoutTransactionInitiatedEvent) {
        transactionId = e.transactionId
        memberId = e.memberId
        shopperReference = e.shopperReference
        transactionStatus = AdyenPayoutTransactionStatus.INITIATED
    }

    @EventSourcingHandler
    fun on(e: AdyenPayoutTransactionAuthorisedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.AUTHORISED
    }

    @EventSourcingHandler
    fun on(e: AdyenPayoutTransactionCanceledEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.CANCELLED
    }

    @EventSourcingHandler
    fun on(e: AdyenPayoutTransactionConfirmedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.AUTHORISED_AND_CONFIRMED
    }

    @CommandHandler
    fun handle(cmd: ReceivedSuccessfulAdyenPayoutTransactionFromNotificationCommand) {
        if (transactionStatus != AdyenPayoutTransactionStatus.SUCCESSFUL) {
            apply(
                SuccessfulAdyenPayoutTransactionReceivedEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.amount
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: SuccessfulAdyenPayoutTransactionReceivedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.SUCCESSFUL
    }

    @CommandHandler
    fun handle(cmd: ReceivedFailedAdyenPayoutTransactionFromNotificationCommand) {
        if (transactionStatus != AdyenPayoutTransactionStatus.CAPTURE_FAILED) {
            apply(
                FailedAdyenPayoutTransactionReceivedEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.amount,
                    cmd.reason
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: FailedAdyenPayoutTransactionReceivedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.CAPTURE_FAILED
    }

    @CommandHandler
    fun handle(cmd: ReceivedDeclinedAdyenPayoutTransactionFromNotificationCommand) {
        if (transactionStatus != AdyenPayoutTransactionStatus.DECLINED) {
            apply(
                DeclinedAdyenPayoutTransactionReceivedEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.amount,
                    cmd.reason
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: DeclinedAdyenPayoutTransactionReceivedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.DECLINED
    }

    @CommandHandler
    fun handle(cmd: ReceivedExpiredAdyenPayoutTransactionFromNotificationCommand) {
        if (transactionStatus != AdyenPayoutTransactionStatus.EXPIRED) {
            apply(
                ExpiredAdyenPayoutTransactionReceivedEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.amount,
                    cmd.reason
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: ExpiredAdyenPayoutTransactionReceivedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.EXPIRED
    }

    @CommandHandler
    fun handle(cmd: ReceivedReservedAdyenPayoutTransactionFromNotificationCommand) {
        if (transactionStatus != AdyenPayoutTransactionStatus.RESERVED) {
            apply(
                ReservedAdyenPayoutTransactionReceivedEvent(
                    cmd.transactionId,
                    cmd.memberId,
                    cmd.amount,
                    cmd.reason
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(e: ReservedAdyenPayoutTransactionReceivedEvent) {
        transactionStatus = AdyenPayoutTransactionStatus.RESERVED
    }
}
