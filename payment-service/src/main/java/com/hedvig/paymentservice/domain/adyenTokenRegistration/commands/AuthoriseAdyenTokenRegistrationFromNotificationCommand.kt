package com.hedvig.paymentservice.domain.adyenTokenRegistration.commands

import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequestItem
import java.util.UUID
import org.axonframework.commandhandling.TargetAggregateIdentifier

class AuthoriseAdyenTokenRegistrationFromNotificationCommand(
    @TargetAggregateIdentifier  
    val adyenTokenRegistrationId: UUID,
    val memberId: String,
    val adyenNotification: NotificationRequestItem,
    val shopperReference: String
)
