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
    fun notifications(@RequestBody requestBody: NotificationRequest): ResponseEntity<String> {
        requestBody.notificationItems!!.forEach { item ->
            try {
                when (item.notificationItem!!.eventCode?.toUpperCase()) {
                    CAPTURE_FAILED -> adyenService.handleSettlementErrorNotification(UUID.fromString(item.notificationItem?.merchantReference!!))
                    AUTHORISATION -> adyenService.handleAuthorisationNotification(item.notificationItem!!)
                    RECURRING_CONTRACT -> adyenService.handleRecurringContractNotification(item.notificationItem!!)
                    PAYOUT_THIRDPARTY -> adyenService.handlePayoutThirdPartyNotification(item.notificationItem!!)
                    PAYOUT_DECLINE -> adyenService.handlePayoutDeclinedNotification(item.notificationItem!!)
                    PAYOUT_EXPIRE -> adyenService.handlePayoutExpireNotification(item.notificationItem!!)
                    PAIDOUT_REVERSED -> adyenService.handlePayoutPaidOutReservedNotification(item.notificationItem!!)
                    AUTORESCUE -> adyenService.handleAutoRescueNotification(item.notificationItem!!)
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
        const val CAPTURE_FAILED = "CAPTURE_FAILED"
        const val AUTHORISATION = "AUTHORISATION"
        const val RECURRING_CONTRACT = "RECURRING_CONTRACT"
        const val PAYOUT_THIRDPARTY = "PAYOUT_THIRDPARTY"
        const val PAYOUT_DECLINE = "PAYOUT_DECLINE"
        const val PAYOUT_EXPIRE = "PAYOUT_EXPIRE"
        const val PAIDOUT_REVERSED = "PAIDOUT_REVERSED"
        const val AUTORESCUE = "AUTORESCUE"
    }
}
