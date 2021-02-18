package com.hedvig.paymentservice.domain.payments.sagas

import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.adyenTransaction.commands.InitiateAdyenTransactionPayoutCommand
import com.hedvig.paymentservice.domain.swish.commands.InitiateSwishTransactionPayoutCommand
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutDetails
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePayoutOrderCommand
import com.hedvig.paymentservice.services.trustly.TrustlyService
import com.hedvig.paymentservice.services.trustly.dto.PayoutRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@Saga
class PayoutSaga {
    @Autowired
    @Transient
    lateinit var commandGateway: CommandGateway

    @Autowired
    @Transient
    lateinit var trustlyService: TrustlyService

    @Autowired
    @Transient
    lateinit var uuidGenerator: UUIDGenerator

    @StartSaga
    @SagaEventHandler(associationProperty = "memberId")
    @EndSaga
    fun on(event: PayoutCreatedEvent) = when (event.payoutDetails) {
        is PayoutDetails.Trustly -> {
            val hedvigOrderId = commandGateway!!.sendAndWait<UUID>(
                CreatePayoutOrderCommand(
                    uuidGenerator.generateRandom(),
                    event.transactionId,
                    event.memberId,
                    event.amount,
                    event.payoutDetails.accountId,
                    event.address,
                    event.countryCode,
                    event.dateOfBirth,
                    event.firstName,
                    event.lastName
                )
            )
            trustlyService.startPayoutOrder(
                PayoutRequest(
                    event.memberId,
                    event.amount,
                    event.payoutDetails.accountId,
                    event.address,
                    event.countryCode,
                    event.dateOfBirth,
                    event.firstName,
                    event.lastName,
                    event.category,
                    event.carrier
                ),
                hedvigOrderId
            )
        }
        is PayoutDetails.Adyen -> {
            commandGateway.sendAndWait<Any>(
                InitiateAdyenTransactionPayoutCommand(
                    event.transactionId,
                    event.memberId,
                    event.payoutDetails.shopperReference,
                    event.amount,
                    event.email ?: ""
                )
            )
        }
        is PayoutDetails.Swish -> commandGateway.sendAndWait<Any>(
            InitiateSwishTransactionPayoutCommand(
                event.transactionId,
                event.memberId,
                event.payoutDetails.phoneNumber,
                event.payoutDetails.ssn,
                event.payoutDetails.message,
                event.amount
            )
        )
    }
}
