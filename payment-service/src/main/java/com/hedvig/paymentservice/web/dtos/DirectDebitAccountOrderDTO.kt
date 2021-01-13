package com.hedvig.paymentservice.web.dtos

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import java.util.*

data class DirectDebitAccountOrderDTO(
    val hedvigOrderId: UUID,
    val memberId: String,
    val trustlyAccountId: String,
    var directDebitStatus: DirectDebitStatus?,
) {
    companion object {
        fun from(d: DirectDebitAccountOrder) = DirectDebitAccountOrderDTO(
            d.hedvigOrderId,
            d.memberId,
            d.trustlyAccountId,
            d.directDebitStatus
        )
    }
}
