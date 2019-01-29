package com.hedvig.paymentservice.domain.trustlyOrder.sagas

import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent
import lombok.`val`
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateNotFoundException
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

@Saga
class AccountCreationSaga {

    @Autowired
    @Transient
    internal var commandGateway: CommandGateway? = null

    @StartSaga
    @SagaEventHandler(associationProperty = "accountId")
    @EndSaga
    fun on(event: AccountNotificationReceivedEvent) {
        try {
            updateTrustlyAccount(event)
        } catch (e: AggregateNotFoundException) {
            commandGateway!!.sendAndWait<Any>(CreateMemberCommand(event.memberId))
            updateTrustlyAccount(event)
        }

    }

    private fun updateTrustlyAccount(event: AccountNotificationReceivedEvent) {
        val command = UpdateTrustlyAccountCommand(
            event.memberId,
            event.hedvigOrderId,
            event.accountId,
            event.address,
            event.bank,
            event.city,
            event.clearingHouse,
            event.descriptor,
            event.directDebitMandate,
            event.lastDigits,
            event.name,
            event.personId,
            event.zipCode
        )

        commandGateway!!.sendAndWait<Any>(command)
    }
}
