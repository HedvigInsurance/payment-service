package com.hedvig.paymentservice.services.swish.web

import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionCompletedCommand
import com.hedvig.paymentservice.domain.swish.commands.SwishPayoutTransactionFailedCommand
import com.hedvig.paymentservice.services.swish.util.SwishUUIDConverter
import com.hedvig.paymentservice.services.swish.dto.Callback
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/swish/"])
class SwishController(
    val commandGateway: CommandGateway
) {

    @PostMapping("callback")
    fun callback(
        @RequestBody callback: Callback
    ) {
        when (callback.status) {
            "PAID" -> {
                commandGateway.sendAndWait<Void>(
                    SwishPayoutTransactionCompletedCommand(
                        SwishUUIDConverter.fromPayoutInstructionUUIDToTransactionId(callback.payoutInstructionUUID),
                        callback.payerPaymentReference
                    )
                )
            }
            else -> {
                commandGateway.sendAndWait<Void>(
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



}

