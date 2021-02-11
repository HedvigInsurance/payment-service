package com.hedvig.paymentservice.domain.payments.events

import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import java.time.Instant
import java.util.UUID
import javax.money.MonetaryAmount
import org.axonframework.serialization.Revision

@Revision("2.0")
data class ChargeCreatedEvent(
    val memberId: String,
    val transactionId: UUID,
    val amount: MonetaryAmount,
    val timestamp: Instant,
    val providerId: String,
    val provider: PayinProvider,
    val email: String,
    var createdBy: String
)
