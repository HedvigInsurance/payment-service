package com.hedvig.paymentservice.domain.adyenTransaction.sagas

import com.adyen.model.checkout.PaymentsResponse
import com.hedvig.paymentservice.domain.adyenTransaction.commands.AuthoriseAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.CancelAdyenTransactionCommand
import com.hedvig.paymentservice.domain.adyenTransaction.commands.ReceivePendingResponseAdyenTransaction
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionCanceledEvent
import com.hedvig.paymentservice.domain.adyenTransaction.events.AdyenTransactionInitiatedEvent
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.adyen.dtos.ChargeMemberWithTokenRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AdyenTransactionSaga {
  @Autowired
  @Transient
  private lateinit var commandGateway: CommandGateway

  @Autowired
  @Transient
  private lateinit var adyenService: AdyenService

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: AdyenTransactionInitiatedEvent) {
    try {
      val request = ChargeMemberWithTokenRequest(e.transactionId, e.memberId, e.recurringDetailReference, e.amount)
      val response = adyenService.chargeMemberWithToken(request)

      when (response.resultCode) {
        PaymentsResponse.ResultCodeEnum.AUTHORISED -> {
          commandGateway.sendAndWait<Void>(
            AuthoriseAdyenTransactionCommand(
              e.transactionId,
              e.memberId,
              e.recurringDetailReference,
              e.amount
            )
          )
        }

        PaymentsResponse.ResultCodeEnum.AUTHENTICATIONFINISHED,
        PaymentsResponse.ResultCodeEnum.AUTHENTICATIONNOTREQUIRED,
        PaymentsResponse.ResultCodeEnum.CHALLENGESHOPPER,
        PaymentsResponse.ResultCodeEnum.IDENTIFYSHOPPER,
        PaymentsResponse.ResultCodeEnum.PENDING,
        PaymentsResponse.ResultCodeEnum.RECEIVED,
        PaymentsResponse.ResultCodeEnum.PARTIALLYAUTHORISED,
        PaymentsResponse.ResultCodeEnum.PRESENTTOSHOPPER,
        PaymentsResponse.ResultCodeEnum.REDIRECTSHOPPER,
        PaymentsResponse.ResultCodeEnum.UNKNOWN -> {
          commandGateway.sendAndWait<Void>(
            ReceivePendingResponseAdyenTransaction(
              e.transactionId,
              e.memberId,
              e.recurringDetailReference,
              e.amount,
              response.resultCode.value
            )
          )
        }

        PaymentsResponse.ResultCodeEnum.CANCELLED,
        PaymentsResponse.ResultCodeEnum.ERROR,
        PaymentsResponse.ResultCodeEnum.REFUSED -> {
          commandGateway.sendAndWait<Void>(
            CancelAdyenTransactionCommand(
              e.transactionId,
              e.memberId,
              e.recurringDetailReference,
              e.amount,
              response.resultCode.value
            )
          )
        }
      }


    } catch (ex: Exception) {
      commandGateway.sendAndWait<Void>(
        CancelAdyenTransactionCommand(
          e.transactionId,
          e.memberId,
          e.recurringDetailReference,
          e.amount,
          ex.message ?: EXCEPTION_MESSAGE
        )
      )
    }
  }

/* Comment until ADYEN will let us know when the charge will be considered final
  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: AdyenTransactionAuthorisedEvent) {
    commandGateway.sendAndWait<Void>(
      ChargeCompletedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId,
        amount = e.amount,
        timestamp = Instant.now()
      )
    )
  }
*/

  @StartSaga
  @SagaEventHandler(associationProperty = TRANSACTION_ID)
  @EndSaga
  fun on(e: AdyenTransactionCanceledEvent) {
    commandGateway.sendAndWait<Void>(
      ChargeFailedCommand(
        memberId = e.memberId,
        transactionId = e.transactionId
      )
    )
  }

  companion object {
    const val TRANSACTION_ID: String = "transactionId"
    const val EXCEPTION_MESSAGE: String = "exception"
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }


}
