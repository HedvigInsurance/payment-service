package com.hedvig.paymentservice.serviceIntergration.notificationService

import com.fasterxml.jackson.databind.JsonNode

interface NotificationService {

  fun updateCustomer(memberId: String, data: Map<String, Any?>)

  fun updateCustomer(memberId: String, data: JsonNode)
}
