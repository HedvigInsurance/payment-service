package com.hedvig.paymentservice.web

import com.adyen.model.notification.NotificationRequest
import com.hedvig.paymentservice.query.adyenNotification.AdyenNotification
import com.hedvig.paymentservice.query.adyenNotification.AdyenNotificationRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
@RequestMapping("/hooks/adyen/")
class AdyenNotificationController(
  val adyenNotificationRepository: AdyenNotificationRepository
) {

  @Value("\${hedvig.adyen.webhook.name}")
  lateinit var name: String

  @Value("\${hedvig.adyen.webhook.password}")
  lateinit var password: String

  @PostMapping(value = ["notifications"], produces = ["application/json"])
  fun notifications(@RequestBody requestBody: NotificationRequest?): ResponseEntity<Void> {
    val authentication = SecurityContextHolder.getContext().authentication

    if (authentication.name != name && authentication.credentials != password) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

    requestBody!!.notificationItemContainers.forEach { item ->
      adyenNotificationRepository.save(
        AdyenNotification.fromNotificationRequestItem(item.notificationItem)
      )
    }

    return ResponseEntity.accepted().build()
  }
}
