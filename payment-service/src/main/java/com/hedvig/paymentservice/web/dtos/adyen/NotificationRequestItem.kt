package com.hedvig.paymentservice.web.dtos.adyen

data class NotificationRequestItem(
    var amount: Amount?,
    val eventCode: String?,
    val eventDate: String?,
    val merchantAccountCode: String?,
    val merchantReference: String?,
    val originalReference: String?,
    val pspReference: String?,
    val reason: String?,
    val success: Boolean = false,
    val paymentMethod: String?,
    val operations: List<String>?,
    val additionalData: Map<String, String>?
)

data class Amount(
    val value: Long?,
    val currency: String?
)
