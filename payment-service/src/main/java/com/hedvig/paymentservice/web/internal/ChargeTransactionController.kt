package com.hedvig.paymentservice.web.internal

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import com.hedvig.paymentservice.web.dtos.CompleteChargeRequestDTO
import com.hedvig.paymentservice.web.dtos.FailChargeRequestDTO
import java.time.Instant
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/adyen/transaction")
class ChargeTransactionController(
    val commandGateway: CommandGateway
) {
  @PostMapping(value = ["charge/complete"])
  fun completeCharge(@RequestBody req: CompleteChargeRequestDTO): ResponseEntity<Void> {
    commandGateway.sendAndWait<Void>(
      ChargeCompletedCommand(
        memberId = req.memberId,
        transactionId = req.transactionId,
        amount = req.amount,
        timestamp = req.timestamp ?: Instant.now()
      )
    )
    return ResponseEntity.accepted().build()
  }

  @PostMapping(value = ["charge/fail"])
  fun failCharge(@RequestBody req: FailChargeRequestDTO): ResponseEntity<Void> {
    commandGateway.sendAndWait<Void>(
      ChargeFailedCommand(
        memberId = req.memberId,
        transactionId = req.transactionId
      )
    )
    return ResponseEntity.accepted().build()
  }
}
