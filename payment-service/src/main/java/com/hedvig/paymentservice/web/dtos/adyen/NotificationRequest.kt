package com.hedvig.paymentservice.web.dtos.adyen

data class NotificationRequest(
    var live: String?,
    val notificationItems: List<NotificationRequestItemContainer>?
)
