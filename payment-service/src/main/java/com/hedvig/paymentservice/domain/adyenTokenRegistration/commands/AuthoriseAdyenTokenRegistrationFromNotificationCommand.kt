package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

class AuthoriseAdyenTokenRegistrationFromNotificationCommand(
  @TargetAggregateIdentifier
  val adyenTokenRegistrationId: UUID,
  val memberId: String,
  val adyenNotification: NotificationRequestItem
)
