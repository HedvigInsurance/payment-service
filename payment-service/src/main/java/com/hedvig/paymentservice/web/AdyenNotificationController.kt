package com.hedvig.paymentservice.web

import com.hedvig.paymentservice.query.adyenNotification.AdyenNotification
import com.hedvig.paymentservice.query.adyenNotification.AdyenNotificationRepository
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.web.dtos.adyen.NotificationRequest
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Slf4j
@RestController
@RequestMapping("/hooks/adyen/")
class AdyenNotificationController(
  val adyenNotificationRepository: AdyenNotificationRepository,
  val adyenService: AdyenService
) {
  @PostMapping(value = ["notifications"], produces = ["application/json"])
  fun notifications(@RequestBody requestBody: NotificationRequest?): ResponseEntity<String> {
    requestBody!!.notificationItems!!.forEach { item ->
      try {
        if (item.notificationItem?.eventCode?.toUpperCase() == CAPTURE_FAILED) {
          adyenService.handleSettlementError(UUID.fromString(item.notificationItem?.merchantReference!!))
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == AUTHORISATION) {
          adyenService.handleAuthorisation(UUID.fromString(item.notificationItem?.merchantReference!!))
        }
      } catch (e: Exception) {
        logger.error("Cannot process notification [Type: $CAPTURE_FAILED] [Exception: $e]")
      }
      adyenNotificationRepository.save(
        AdyenNotification.fromNotificationRequestItem(item.notificationItem)
      )
    }
    return ResponseEntity.ok("[accepted]")
  }

  companion object {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
    const val CAPTURE_FAILED = "CAPTURE_FAILED"
    const val AUTHORISATION = "AUTHORISATION"
  }
}
