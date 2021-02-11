package com.hedvig.paymentservice.serviceIntergration.notificationService

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class NotificationServiceImpl(
    private val notificationServiceClient: NotificationServiceClient,
    private val objectMapper: ObjectMapper
) : NotificationService {
  override fun updateCustomer(memberId: String, data: JsonNode) {
    notificationServiceClient.updateCustomer(memberId, data)
  }

  override fun updateCustomer(memberId: String, data: Map<String, Any?>) {
    notificationServiceClient.updateCustomer(memberId, objectMapper.valueToTree(data))
  }
}
