package com.hedvig.paymentservice.serviceIntergration.accountService

import org.springframework.http.ResponseEntity
import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

interface AccountService {
  fun notifyChargeFailed(
    memberId: String,
    transactionId: UUID
  ): ResponseEntity<Void>

  fun notifyChargeCompleted(
    memberId: String,
    transactionId: UUID,
    amount: MonetaryAmount,
    chargedAt: Instant
  ): ResponseEntity<Void>
}
