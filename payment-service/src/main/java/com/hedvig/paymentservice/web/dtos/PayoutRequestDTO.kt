package com.hedvig.paymentservice.web.dtos

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.enums.Carrier
import javax.money.MonetaryAmount

data class PayoutRequestDTO(
    val amount: MonetaryAmount,
    val sanctionBypassed: Boolean,
    val category: TransactionCategory?,
    val referenceId: String?,
    val note: String?,
    val handler: String?,
    val carrier: Carrier?,
    val payoutDetails: SelectedPayoutDetails?
)
