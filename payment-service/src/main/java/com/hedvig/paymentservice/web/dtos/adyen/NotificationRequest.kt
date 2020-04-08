package com.hedvig.paymentservice.web.dtos.adyen

import com.adyen.model.notification.NotificationRequestItemContainer

class NotificationRequest(
  var live: String?,
  val notificationItems: List<NotificationRequestItemContainer>?
)
