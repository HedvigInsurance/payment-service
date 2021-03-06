package com.hedvig.paymentservice.serviceIntergration.accountService

import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount
import org.springframework.http.ResponseEntity

interface AccountService {
  fun notifyChargeFailed(
      memberId: String,
      transactionId: UUID,
      failedAt: Instant
  ): ResponseEntity<Void>

  fun notifyChargeCompleted(
      memberId: String,
      transactionId: UUID,
      amount: MonetaryAmount,
      chargedAt: Instant
  ): ResponseEntity<Void>

  fun notifyBackfilledChargeCompleted(
      memberId: String,
      transactionId: UUID,
      amount: MonetaryAmount,
      chargedAt: Instant
  ): ResponseEntity<Void>

  fun notifyChargeInitiated(
      memberId: String,
      transactionId: UUID,
      amount: MonetaryAmount,
      initiatedBy: String?,
      createdAt: Instant
  ): ResponseEntity<Void>
}
