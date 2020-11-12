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
          adyenService.handleSettlementErrorNotification(UUID.fromString(item.notificationItem?.merchantReference!!))
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == AUTHORISATION) {
          adyenService.handleAuthorisationNotification(item.notificationItem!!)
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == RECURRING_CONTRACT) {
          adyenService.handleRecurringContractNotification(item.notificationItem!!)
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == PAYOUT_THIRDPARTY) {
          adyenService.handlePayoutThirdPartyNotification(item.notificationItem!!)
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == PAYOUT_DECLINE) {
          adyenService.handlePayoutDeclinedNotification(item.notificationItem!!)
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == PAYOUT_EXPIRE) {
          adyenService.handlePayoutExpireNotification(item.notificationItem!!)
        }
        if (item.notificationItem?.eventCode?.toUpperCase() == PAIDOUT_REVERSED) {
          adyenService.handlePayoutPaidOutReservedNotification(item.notificationItem!!)
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
    const val RECURRING_CONTRACT = "RECURRING_CONTRACT"
    const val PAYOUT_THIRDPARTY = "PAYOUT_THIRDPARTY"
    const val PAYOUT_DECLINE = "PAYOUT_DECLINE"
    const val PAYOUT_EXPIRE = "PAYOUT_EXPIRE"
    const val PAIDOUT_REVERSED = "PAIDOUT_REVERSED"
  }
}
