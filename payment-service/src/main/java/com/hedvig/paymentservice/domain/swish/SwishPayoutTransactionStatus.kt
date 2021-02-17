package com.hedvig.paymentservice.domain.swish

enum class SwishPayoutTransactionStatus {
    INITIATED,
    CANCELLED,
    CONFIRMED,
    COMPLETED,
    FAILED
}
