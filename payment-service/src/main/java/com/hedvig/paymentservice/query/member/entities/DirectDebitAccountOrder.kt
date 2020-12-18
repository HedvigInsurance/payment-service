package com.hedvig.paymentservice.query.member.entities

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class DirectDebitAccountOrder(
    @Id
    val hedvigOrderId: UUID,
    val memberId: String,
    val trustlyAccountId: String,
    val bank: String,
    val descriptor: String,
    val directDebitStatus: DirectDebitStatus,
    val createdAt: Instant
)
