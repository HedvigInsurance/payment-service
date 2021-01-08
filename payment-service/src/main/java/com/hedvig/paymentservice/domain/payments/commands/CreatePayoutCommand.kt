package com.hedvig.paymentservice.domain.payments.commands

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.money.MonetaryAmount

data class CreatePayoutCommand(
  @TargetAggregateIdentifier
  val memberId: String,
  val address: String?,
  val countryCode: String?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val lastName: String?,
  val transactionId: UUID,
  val amount: MonetaryAmount,
  val timestamp: Instant,
  val category: TransactionCategory,
  val referenceId: String?,
  val note: String?,
  val handler: String?,
  val email: String
)
