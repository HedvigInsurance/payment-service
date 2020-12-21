package com.hedvig.paymentservice.query.directDebit

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class DirectDebitAccountOrder(
    @Id
    val hedvigOrderId: UUID,
    val memberId: String,
    val trustlyAccountId: String,
    val bank: String?,
    val descriptor: String?,
    @Enumerated(EnumType.STRING)
    var directDebitStatus: DirectDebitStatus?,
    val createdAt: Instant
) {

    companion object {
        fun fromTrustlyAccountCreatedEvent(event: TrustlyAccountCreatedEvent, timestamp: Instant) =
            DirectDebitAccountOrder(
                hedvigOrderId = event.hedvigOrderId,
                memberId = event.memberId,
                trustlyAccountId = event.trustlyAccountId,
                bank = event.bank,
                descriptor = event.descriptor,
                directDebitStatus = null,
                createdAt = timestamp
            )

        fun fromTrustlyAccountUpdatedEvent(event: TrustlyAccountUpdatedEvent, timestamp: Instant) =
            DirectDebitAccountOrder(
                hedvigOrderId = event.hedvigOrderId,
                memberId = event.memberId,
                trustlyAccountId = event.trustlyAccountId,
                bank = event.bank,
                descriptor = event.descriptor,
                directDebitStatus = null,
                createdAt = timestamp
            )
    }
}
