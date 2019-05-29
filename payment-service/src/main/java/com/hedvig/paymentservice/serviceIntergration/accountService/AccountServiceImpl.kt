package com.hedvig.paymentservice.serviceIntergration.accountService

import com.hedvig.paymentservice.serviceIntergration.accountService.dto.NotifyChargeCompletedRequestDto
import com.hedvig.paymentservice.serviceIntergration.accountService.dto.NotifyChargeFailedRequestDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

@Service
class AccountServiceImpl @Autowired constructor(
  private val accountServiceClient: AccountServiceClient
): AccountService {
  override fun notifyChargeFailed(memberId: String, transactionId: UUID): ResponseEntity<Void> {
    val request = NotifyChargeFailedRequestDto(transactionId)
    return accountServiceClient.notifyChargeFailed(memberId, request)
  }

  override fun notifyChargeCompleted(memberId: String, transactionId: UUID, amount: MonetaryAmount, chargedAt: Instant): ResponseEntity<Void> {
    val request = NotifyChargeCompletedRequestDto(
      transactionId = transactionId,
      amount = amount,
      chargedAt = chargedAt
    )
    return accountServiceClient.notifyChargeCompleted(memberId, request)
  }
}
