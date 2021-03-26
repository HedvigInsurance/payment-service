package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.enums.Carrier
import org.axonframework.serialization.Revision
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.money.MonetaryAmount

@Revision("3.0")
data class PayoutCreatedEvent(
    val memberId: String,
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val address: String?,
    val countryCode: String?,
    val dateOfBirth: LocalDate?,
    val firstName: String?,
    val lastName: String?,
    val timestamp: Instant,
    val category: TransactionCategory,
    val referenceId: String?,
    val note: String?,
    val handler: String?,
    val email: String?,
    val carrier: Carrier?,
    val payoutDetails: PayoutDetails
)

