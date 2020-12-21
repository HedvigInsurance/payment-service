package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenPayoutAccountCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus.Companion.fromTokenRegistrationStatus
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenPayoutAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenPayoutAccountUpdatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeErroredEvent
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitConnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitDisconnectedEvent
import com.hedvig.paymentservice.domain.payments.events.DirectDebitPendingConnectionEvent
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutErroredEvent
import com.hedvig.paymentservice.domain.payments.events.PayoutFailedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventhandling.Timestamp
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import javax.money.MonetaryAmount

@Aggregate
class Member() {
    @AggregateIdentifier
    lateinit var memberId: String
    val directDebitAccountOrders: MutableList<DirectDebitAccountOrder> = mutableListOf()
    var transactions: MutableList<Transaction> = ArrayList()
    var adyenAccount: AdyenAccount? = null
    var adyenPayoutAccount: AdyenPayoutAccount? = null

    @CommandHandler
    constructor(
        command: CreateMemberCommand
    ) : this() {
        apply(MemberCreatedEvent(command.memberId))
    }

    @CommandHandler
    fun handle(command: CreateChargeCommand, productPricingService: ProductPricingService): ChargeMemberResult {
        val contractMarketInfo = productPricingService.getContractMarketInfo(command.memberId)
        if (contractMarketInfo.preferredCurrency != command.amount.currency) {
            log.error("Currency mismatch while charging [MemberId: $command.memberId] [PreferredCurrency: ${contractMarketInfo.preferredCurrency}] [RequestCurrency: ${command.amount.currency}]")
            failChargeCreation(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp,
                reason = "currency mismatch"
            )
            return ChargeMemberResult(command.transactionId, ChargeMemberResultType.CURRENCY_MISMATCH)
        }

        val trustlyAccount = getTrustlyAccountBasedOnLatestHedvigOrder()

        if (trustlyAccount == null && adyenAccount == null) {
            log.info("Cannot charge account - no account set up ${command.memberId}")
            failChargeCreation(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp,
                reason = "no payin method found"
            )
            return ChargeMemberResult(command.transactionId, ChargeMemberResultType.NO_PAYIN_METHOD_FOUND)
        }

        if (trustlyAccount != null && trustlyAccount.directDebitStatus != DirectDebitStatus.CONNECTED) {
            log.info("Cannot charge account - direct debit mandate not received in Trustly ${command.memberId}")
            failChargeCreation(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp,
                reason = "direct debit mandate not received in Trustly"
            )
            return ChargeMemberResult(command.transactionId, ChargeMemberResultType.NO_DIRECT_DEBIT)
        }

        if (adyenAccount != null && adyenAccount!!.status != AdyenAccountStatus.AUTHORISED) {
            log.info("Cannot charge account - adyen recurring status is not authorised ${command.memberId}")
            failChargeCreation(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp,
                reason = "adyen recurring is not authorised"
            )
            return ChargeMemberResult(command.transactionId, ChargeMemberResultType.ADYEN_NOT_AUTHORISED)
        }

        val provider = when {
            trustlyAccount != null -> PayinProvider.TRUSTLY
            adyenAccount?.recurringDetailReference != null -> PayinProvider.ADYEN
            else -> throw IllegalStateException("CreateChargeCommand failed. Cannot find provider. [MemberId: $memberId] [TransactionId: ${command.transactionId}]")
        }

        apply(
            ChargeCreatedEvent(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp,
                providerId = when (provider) {
                    PayinProvider.TRUSTLY -> trustlyAccount!!.accountId
                    PayinProvider.ADYEN -> adyenAccount!!.recurringDetailReference
                },
                provider = provider,
                email = command.email,
                createdBy = command.createdBy
            )
        )
        return ChargeMemberResult(command.transactionId, ChargeMemberResultType.SUCCESS)
    }

    @CommandHandler
    fun handle(command: CreatePayoutCommand): Boolean {
        if (getTrustlyAccountBasedOnLatestHedvigOrder() != null) {
            val trustlyAccount = getTrustlyAccountBasedOnLatestHedvigOrder()
            apply(
                PayoutCreatedEvent(
                    memberId = memberId,
                    transactionId = command.transactionId,
                    amount = command.amount,
                    address = command.address,
                    countryCode = command.countryCode,
                    dateOfBirth = command.dateOfBirth,
                    firstName = command.firstName,
                    lastName = command.lastName,
                    timestamp = command.timestamp,
                    trustlyAccountId = trustlyAccount!!.accountId,
                    category = command.category,
                    referenceId = command.referenceId,
                    note = command.note,
                    handler = command.handler,
                    adyenShopperReference = null,
                    email = command.email
                )
            )
            return true
        }

        adyenPayoutAccount?.let { account ->
            PayoutCreatedEvent(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                address = command.address,
                countryCode = command.countryCode,
                dateOfBirth = command.dateOfBirth,
                firstName = command.firstName,
                lastName = command.lastName,
                timestamp = command.timestamp,
                category = command.category,
                referenceId = command.referenceId,
                note = command.note,
                handler = command.handler,
                adyenShopperReference = account.shopperReference,
                trustlyAccountId = null,
                email = command.email
            )
            return true
        }

        log.info("Cannot payout account - no payout account is set up")
        apply(
            PayoutCreationFailedEvent(memberId, command.transactionId, command.amount, command.timestamp)
        )
        return false
    }

    @CommandHandler
    fun handle(command: UpdateTrustlyAccountCommand) {
        if (directDebitAccountOrders.isEmpty() || directDebitAccountOrders.none { it.account.accountId == command.accountId }) {
            apply(
                TrustlyAccountCreatedEvent.fromUpdateTrustlyAccountCommand(memberId, command)
            )
        } else {
            apply(
                TrustlyAccountUpdatedEvent.fromUpdateTrustlyAccountCommand(memberId, command)
            )
        }
        updateDirectDebitStatus(command)
    }

    @CommandHandler
    fun handle(command: UpdateAdyenAccountCommand) {
        if (adyenAccount == null || adyenAccount!!.recurringDetailReference != command.recurringDetailReference) {
            apply(
                AdyenAccountCreatedEvent(
                    command.memberId,
                    command.recurringDetailReference,
                    fromTokenRegistrationStatus(command.adyenTokenStatus)
                )
            )
        } else {
            apply(
                AdyenAccountUpdatedEvent(
                    command.memberId,
                    command.recurringDetailReference,
                    fromTokenRegistrationStatus(command.adyenTokenStatus)
                )
            )
        }
    }

    @CommandHandler
    fun handle(command: UpdateAdyenPayoutAccountCommand) {
        if (adyenPayoutAccount == null) {
            apply(
                AdyenPayoutAccountCreatedEvent(
                    command.memberId,
                    command.shopperReference,
                    fromTokenRegistrationStatus(command.adyenTokenStatus)
                )
            )
        } else {
            apply(
                AdyenPayoutAccountUpdatedEvent(
                    command.memberId,
                    command.shopperReference,
                    fromTokenRegistrationStatus(command.adyenTokenStatus)
                )
            )
        }
    }

    @CommandHandler
    fun handle(command: ChargeCompletedCommand) {
        val transaction = getSingleTransaction(command.transactionId)
        if (transaction.amount != command.amount) {
            log.error(
                "CRITICAL: Transaction amounts differ for transactionId: ${transaction.transactionId} " +
                    "- our amount: ${transaction.amount}, " +
                    "amount from payment provider: ${command.amount}"
            )
            apply(
                ChargeErroredEvent(
                    command.memberId,
                    command.transactionId,
                    command.amount,
                    "Transaction amounts differ (expected ${transaction.amount} but was ${command.amount})",
                    command.timestamp
                )
            )
            throw RuntimeException("Transaction amount mismatch")
        }
        apply(
            ChargeCompletedEvent(
                memberId, command.transactionId, command.amount, command.timestamp
            )
        )
    }

    @CommandHandler
    fun handle(command: ChargeFailedCommand) {
        getSingleTransaction(command.transactionId)
        apply(
            ChargeFailedEvent(
                memberId,
                command.transactionId
            )
        )
    }

    @CommandHandler
    fun handle(command: PayoutCompletedCommand) {
        val transaction = getSingleTransaction(command.transactionId)
        if (transaction.amount != command.amount) {
            log.error(
                "CRITICAL: Transaction amounts differ for transactionId: ${transaction.transactionId} " +
                    "- our amount: ${transaction.amount}, " +
                    "amount from payment provider: ${command.amount}"
            )
            apply(
                PayoutErroredEvent(
                    memberId = command.memberId,
                    transactionId = command.transactionId,
                    amount = command.amount,
                    reason = "Transaction amounts differ (expected ${transaction.amount} but was ${command.amount})",
                    timestamp = command.timestamp
                )
            )
            throw RuntimeException("Transaction amount mismatch")
        }
        apply(
            PayoutCompletedEvent(
                memberId = memberId,
                transactionId = command.transactionId,
                timestamp = command.timestamp
            )
        )
    }

    @CommandHandler
    fun handle(command: PayoutFailedCommand) {
        apply(
            PayoutFailedEvent(
                memberId = memberId,
                transactionId = command.transactionId,
                amount = command.amount,
                timestamp = command.timestamp
            )
        )
    }

    @EventSourcingHandler
    fun on(event: MemberCreatedEvent) {
        memberId = event.memberId
    }

    @EventSourcingHandler
    fun on(event: ChargeCreatedEvent) {
        val tx = Transaction(event.transactionId, event.amount, event.timestamp)
        tx.transactionType = TransactionType.CHARGE
        tx.transactionStatus = TransactionStatus.INITIATED
        transactions.add(tx)
    }

    @EventSourcingHandler
    fun on(event: PayoutCreatedEvent) {
        val tx = Transaction(event.transactionId, event.amount, event.timestamp)
        tx.transactionType = TransactionType.PAYOUT
        tx.transactionStatus = TransactionStatus.INITIATED
        transactions.add(tx)
    }

    @EventSourcingHandler
    fun on(event: ChargeCompletedEvent) {
        val tx = getSingleTransaction(event.transactionId)
        tx.transactionStatus = TransactionStatus.COMPLETED
    }

    @EventSourcingHandler
    fun on(event: ChargeFailedEvent) {
        val tx = getSingleTransaction(event.transactionId)
        tx.transactionStatus = TransactionStatus.FAILED
    }

    @EventSourcingHandler
    fun on(event: PayoutCompletedEvent) {
        val tx = getSingleTransaction(event.transactionId)
        tx.transactionStatus = TransactionStatus.COMPLETED
    }

    @EventSourcingHandler
    fun on(event: PayoutFailedEvent) {
        val transaction = getSingleTransaction(event.transactionId)
        transaction.transactionStatus = TransactionStatus.FAILED
    }

    @EventSourcingHandler
    fun on(event: TrustlyAccountCreatedEvent, @Timestamp timestamp: Instant) {
        directDebitAccountOrders.add(
            DirectDebitAccountOrder(
                event.hedvigOrderId,
                TrustlyAccount(event.trustlyAccountId, null),
                timestamp
            )
        )
    }

    @EventSourcingHandler
    fun on(event: TrustlyAccountUpdatedEvent, @Timestamp timestamp: Instant) {
        if (directDebitAccountOrders.none { it.hedvigOrderId == event.hedvigOrderId }) {
            directDebitAccountOrders.add(
                DirectDebitAccountOrder(
                    event.hedvigOrderId,
                    TrustlyAccount(event.trustlyAccountId, null),
                    timestamp
                )
            )
        }
    }

    @EventSourcingHandler
    fun on(event: DirectDebitConnectedEvent) {
        setTrustlyAccountStatus(event.hedvigOrderId, DirectDebitStatus.CONNECTED)
    }

    @EventSourcingHandler
    fun on(event: DirectDebitDisconnectedEvent) {
        setTrustlyAccountStatus(event.hedvigOrderId, DirectDebitStatus.DISCONNECTED)
    }

    @EventSourcingHandler
    fun on(event: DirectDebitPendingConnectionEvent) {
        setTrustlyAccountStatus(event.hedvigOrderId, DirectDebitStatus.PENDING)
    }

    @EventSourcingHandler
    fun on(event: AdyenAccountCreatedEvent) {
        adyenAccount = AdyenAccount(
            event.recurringDetailReference,
            event.accountStatus
        )
    }

    @EventSourcingHandler
    fun on(event: AdyenPayoutAccountCreatedEvent) {
        adyenPayoutAccount = AdyenPayoutAccount(
            event.shopperReference,
            event.accountStatus
        )
    }

    @EventSourcingHandler
    fun on(event: AdyenAccountUpdatedEvent) {
        adyenAccount = AdyenAccount(
            event.recurringDetailReference,
            event.accountStatus
        )
    }

    @EventSourcingHandler
    fun on(event: AdyenPayoutAccountUpdatedEvent) {
        adyenPayoutAccount = AdyenPayoutAccount(
            event.shopperReference,
            event.accountStatus
        )
    }

    private fun updateDirectDebitStatus(command: UpdateTrustlyAccountCommand) {
        if (command.directDebitMandateActive != null && command.directDebitMandateActive) {
            apply(
                DirectDebitConnectedEvent(
                    memberId = memberId,
                    hedvigOrderId = command.hedvigOrderId.toString(),
                    trustlyAccountId = command.accountId
                )
            )
        } else if (command.directDebitMandateActive != null && !command.directDebitMandateActive) {
            apply(
                DirectDebitDisconnectedEvent(
                    memberId = memberId,
                    hedvigOrderId = command.hedvigOrderId.toString(),
                    trustlyAccountId = command.accountId
                )
            )
        } else {
            val trustlyAccount = getTrustlyAccountBasedOnLatestHedvigOrder()

            if (trustlyAccount!!.directDebitStatus == null ||
                (
                    trustlyAccount.directDebitStatus != DirectDebitStatus.CONNECTED ||
                        trustlyAccount.directDebitStatus != DirectDebitStatus.DISCONNECTED
                    )
            ) {
                apply(
                    DirectDebitPendingConnectionEvent(
                        memberId = memberId,
                        hedvigOrderId = command.hedvigOrderId.toString(),
                        trustlyAccountId = command.accountId
                    )
                )
            }
        }
    }

    private fun failChargeCreation(
        memberId: String,
        transactionId: UUID,
        amount: MonetaryAmount,
        timestamp: Instant,
        reason: String
    ) {
        apply(
            ChargeCreationFailedEvent(
                memberId,
                transactionId,
                amount,
                timestamp,
                reason
            )
        )
    }

    private fun getSingleTransaction(
        transactionId: UUID
    ): Transaction {
        val matchingTransactions = transactions.filter { transaction -> transaction.transactionId == transactionId }
        if (matchingTransactions.size != 1) {
            throw RuntimeException(
                "Unexpected number of matching transactions: ${matchingTransactions.size}, with transactionId: $transactionId for memberId: $memberId"
            )
        }
        return matchingTransactions[0]
    }

    private fun setTrustlyAccountStatus(hedvigOrderId: String, status: DirectDebitStatus) {
        directDebitAccountOrders
            .first { it.hedvigOrderId == UUID.fromString(hedvigOrderId) }
            .account
            .directDebitStatus = status
    }

    private fun getTrustlyAccountBasedOnLatestHedvigOrder() = directDebitAccountOrders.maxByOrNull { it.createdAt }?.account


    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }
}
