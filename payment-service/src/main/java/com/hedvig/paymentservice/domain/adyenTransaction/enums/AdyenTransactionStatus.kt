package com.hedvig.paymentservice.domain.adyenTransaction.enums

enum class AdyenTransactionStatus {
    INITIATED,
    PENDING,
    AUTHORISED,
    CANCELLED,
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
