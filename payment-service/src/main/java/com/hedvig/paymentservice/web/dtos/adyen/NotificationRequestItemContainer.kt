package com.hedvig.paymentservice.web.dtos.adyen


import com.adyen.model.notification.NotificationRequestItem
import com.fasterxml.jackson.annotation.JsonProperty

class NotificationRequestItemContainer(
  @JsonProperty("NotificationRequestItem")
  var notificationItem: NotificationRequestItem?
)
