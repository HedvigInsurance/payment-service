package com.hedvig.paymentservice.services.swish

import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionCompletedCommand
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionFailedCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.javamoney.moneta.Money
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping(path = ["/swish/"])
class SwishController(
    val swishService: SwishService,
    val commandGateway: CommandGateway
) {

    @GetMapping("payout")
    fun payout() {
        swishService.startPayout(
            transactionId = UUID.randomUUID(),
            memberId = "1234",
            payeeAlias = "46726738711",
            payeeSSN = "198607209882",
            amount = Money.of(10, "SEK"),
            message = "message",
            instructionDate = LocalDateTime.now(),
        )
    }

    @PostMapping("callback")
    fun callback(callback: Callback) {
        when (callback.status) {
            "PAID" -> {
                commandGateway.sendAndWait<Any>(
                    SwishPayoutTransactionCompletedCommand(
                        SwishUUIDConverter.fromPayoutInstructionUUIDToTransactionId(callback.payoutInstructionUUID),
                        callback.payerPaymentReference
                    )
                )
            }
            else -> {
                commandGateway.sendAndWait<Any>(
                    SwishPayoutTransactionFailedCommand(
                        SwishUUIDConverter.fromPayoutInstructionUUIDToTransactionId(callback.payoutInstructionUUID),
                        callback.payerPaymentReference,
                        callback.errorCode,
                        callback.errorMessage,
                        callback.additionalInformation
                    )
                )
            }
        }
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    data class Callback(
        val payoutInstructionUUID: String,
        val payerPaymentReference: String,
        val callbackUrl: String,
        val payerAlias: String,
        val payeeAlias: String,
        val payeeSSN: String,
        val amount: String,
        val currency: String,
        val message: String,
        val status: String,
        val payoutType: String,
        val dateCreated: LocalDateTime,
        val datePaid: LocalDateTime,
        val errorCode: String?,
        val errorMessage: String?,
        val additionalInformation: String?
    )
}
