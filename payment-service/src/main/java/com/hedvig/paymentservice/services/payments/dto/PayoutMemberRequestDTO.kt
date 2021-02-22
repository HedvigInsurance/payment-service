package com.hedvig.paymentservice.services.payments.dto

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.enums.Carrier
import javax.money.MonetaryAmount

data class PayoutMemberRequestDTO(
    val amount: MonetaryAmount,
    val category: TransactionCategory,
    val referenceId: String?,
    val note: String?,
    val handler: String?,
    val carrier: Carrier?
)
