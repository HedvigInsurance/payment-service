package com.hedvig.paymentservice.web.dtos.adyen

import com.fasterxml.jackson.annotation.JsonProperty

data class NotificationRequestItemContainer(
  @JsonProperty("NotificationRequestItem")
  var notificationItem: NotificationRequestItem?
)
