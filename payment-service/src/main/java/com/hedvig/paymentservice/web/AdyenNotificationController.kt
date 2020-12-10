package com.hedvig.paymentservice.web

import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_AUTHORISATION
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_AUTORESCUE
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_CAPTURE_FAILED
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_PAIDOUT_REVERSED
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_PAYOUT_DECLINE
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_PAYOUT_EXPIRE
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_PAYOUT_THIRDPARTY
import com.adyen.model.notification.NotificationRequestItem.EVENT_CODE_RECURRING_CONTRACT
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
    fun notifications(@RequestBody requestBody: NotificationRequest): ResponseEntity<String> {
        requestBody.notificationItems!!.forEach { item ->
            try {
                when (item.notificationItem!!.eventCode?.toUpperCase()) {
                    EVENT_CODE_CAPTURE_FAILED -> adyenService.handleSettlementErrorNotification(UUID.fromString(item.notificationItem?.merchantReference!!))
                    EVENT_CODE_AUTHORISATION -> adyenService.handleAuthorisationNotification(item.notificationItem!!)
                    EVENT_CODE_RECURRING_CONTRACT -> adyenService.handleRecurringContractNotification(item.notificationItem!!)
                    EVENT_CODE_PAYOUT_THIRDPARTY -> adyenService.handlePayoutThirdPartyNotification(item.notificationItem!!)
                    EVENT_CODE_PAYOUT_DECLINE -> adyenService.handlePayoutDeclinedNotification(item.notificationItem!!)
                    EVENT_CODE_PAYOUT_EXPIRE -> adyenService.handlePayoutExpireNotification(item.notificationItem!!)
                    EVENT_CODE_PAIDOUT_REVERSED -> adyenService.handlePayoutPaidOutReservedNotification(item.notificationItem!!)
                    EVENT_CODE_AUTORESCUE -> adyenService.handleAutoRescueNotification(item.notificationItem!!)
                    else -> throw IllegalArgumentException("NotificationItem with eventCode=${item.notificationItem!!.eventCode} is not supported")
                }
            } catch (exception: Exception) {
                logger.error("Cannot process notification [Type: ${item.notificationItem?.eventCode}]", exception)
            }
            adyenNotificationRepository.save(
                AdyenNotification.fromNotificationRequestItem(item.notificationItem)
            )
        }
        return ResponseEntity.ok("[accepted]")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
