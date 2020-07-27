package com.hedvig.paymentservice.domain.adyenTokenRegistration.events

import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import java.util.UUID

data class AdyenTokenRegistrationAuthorisedFromNotificationEvent(
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val notificationRequestItem: NotificationRequestItem
)
