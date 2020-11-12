package com.hedvig.paymentservice.domain.adyenTransaction.enums

enum class AdyenTransactionStatus {
    AUTHORISED,
    PENDING,
    CANCELLED,
    INITIATED,
    CAPTURE_FAILED
}

enum class AdyenPayoutTransactionStatus {
    AUTHORISED,
    CANCELLED,
    INITIATED,
    CAPTURE_FAILED,
    AUTHORISED_AND_CONFIRMED,
    SUCCESSFUL,
    DECLINED,
    EXPIRED,
    RESERVED,
    FAILED
}
