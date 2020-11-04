package com.hedvig.paymentservice.domain.payments

import com.hedvig.paymentservice.domain.payments.commands.ChargeCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.ChargeFailedCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutCompletedCommand
import com.hedvig.paymentservice.domain.payments.commands.PayoutFailedCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateAdyenAccountCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus.Companion.fromTokenRegistrationStatus
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent
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
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountUpdatedEvent.Companion.fromUpdateTrustlyAccountCmd
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle.apply
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.spring.stereotype.Aggregate
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.ArrayList
import java.util.UUID
import java.util.stream.Collectors
import javax.money.MonetaryAmount

@Aggregate
class Member() {
  @AggregateIdentifier
  lateinit var id: String

  var transactions: MutableList<Transaction> = ArrayList()
  var latestTrustlyAccountId: String? = null
  var trustlyAccounts: MutableMap<String, DirectDebitStatus?> = mutableMapOf()
  var adyenAccount: AdyenAccount? = null

  @CommandHandler
  constructor(
    cmd: CreateMemberCommand
  ) : this() {
    apply(
      MemberCreatedEvent(
        cmd.memberId
      )
    )
  }

  @CommandHandler
  fun cmd(cmd: CreateChargeCommand, productPricingService: ProductPricingService): ChargeMemberResult {
    val contractMarketInfo = productPricingService.getContractMarketInfo(cmd.memberId)
    if (contractMarketInfo.preferredCurrency != cmd.amount.currency) {
      log.error("Currency mismatch while charging [MemberId: $cmd.memberId] [PreferredCurrency: ${contractMarketInfo.preferredCurrency}] [RequestCurrency: ${cmd.amount.currency}]")
      failChargeCreation(
        memberId = id,
        transactionId = cmd.transactionId,
        amount = cmd.amount,
        timestamp = cmd.timestamp,
        reason = "currency mismatch"
      )
      return ChargeMemberResult(cmd.transactionId, ChargeMemberResultType.CURRENCY_MISMATCH)
    }

    if (trustlyAccounts.isEmpty() && adyenAccount == null) {
      log.info("Cannot charge account - no account set up ${cmd.memberId}")
      failChargeCreation(
        memberId = id,
        transactionId = cmd.transactionId,
        amount = cmd.amount,
        timestamp = cmd.timestamp,
        reason = "no payin method found"
      )
      return ChargeMemberResult(cmd.transactionId, ChargeMemberResultType.NO_PAYIN_METHOD_FOUND)
    }

    if (trustlyAccounts.isNotEmpty() && trustlyAccounts[latestTrustlyAccountId] != DirectDebitStatus.CONNECTED) {
      log.info("Cannot charge account - direct debit mandate not received in Trustly ${cmd.memberId}")
      failChargeCreation(
        memberId = id,
        transactionId = cmd.transactionId,
        amount = cmd.amount,
        timestamp = cmd.timestamp,
        reason = "direct debit mandate not received in Trustly"
      )
      return ChargeMemberResult(cmd.transactionId, ChargeMemberResultType.NO_DIRECT_DEBIT)
    }

    if (adyenAccount != null && adyenAccount!!.status != AdyenAccountStatus.AUTHORISED) {
      log.info("Cannot charge account - adyen recurring status is not authorised ${cmd.memberId}")
      failChargeCreation(
        memberId = id,
        transactionId = cmd.transactionId,
        amount = cmd.amount,
        timestamp = cmd.timestamp,
        reason = "adyen recurring is not authorised"
      )
      return ChargeMemberResult(cmd.transactionId, ChargeMemberResultType.ADYEN_NOT_AUTHORISED)
    }

    apply(
      ChargeCreatedEvent(
        memberId = id,
        transactionId = cmd.transactionId,
        amount = cmd.amount,
        timestamp = cmd.timestamp,
        providerId = latestTrustlyAccountId ?: adyenAccount!!.recurringDetailReference,
        provider = if (trustlyAccounts.isNotEmpty()) PayinProvider.TRUSTLY else PayinProvider.ADYEN,
        email = cmd.email,
        createdBy = cmd.createdBy
      )
    )
    return ChargeMemberResult(cmd.transactionId, ChargeMemberResultType.SUCCESS)
  }

  @CommandHandler
  fun cmd(cmd: CreatePayoutCommand): Boolean {
    if (trustlyAccounts.isEmpty()) {
      log.info("Cannot payout account - no account set up in Trustly")
      apply(
        PayoutCreationFailedEvent(id, cmd.transactionId, cmd.amount, cmd.timestamp)
      )
      return false
    }
    apply(
      PayoutCreatedEvent(
        id,
        cmd.transactionId,
        cmd.amount,
        cmd.address,
        cmd.countryCode,
        cmd.dateOfBirth,
        cmd.firstName,
        cmd.lastName,
        cmd.timestamp,
        latestTrustlyAccountId!!,
        cmd.category,
        cmd.referenceId,
        cmd.note,
        cmd.handler
      )
    )
    return true
  }

  @CommandHandler
  fun cmd(cmd: UpdateTrustlyAccountCommand) {
    if (trustlyAccounts.isEmpty() ||
      (trustlyAccounts.isNotEmpty()
        && latestTrustlyAccountId!! != cmd.accountId
        && !trustlyAccounts.containsKey(latestTrustlyAccountId!!))
    ) {
      apply(
        TrustlyAccountCreatedEvent.fromUpdateTrustlyAccountCmd(
          id,
          cmd
        )
      )
    } else {
      apply(
        fromUpdateTrustlyAccountCmd(
          id,
          cmd
        )
      )
    }
    updateDirectDebitStatus(cmd)
  }

  @CommandHandler
  fun cmd(cmd: UpdateAdyenAccountCommand) {
    if (adyenAccount == null || adyenAccount!!.recurringDetailReference != cmd.recurringDetailReference) {
      apply(
        AdyenAccountCreatedEvent(
          cmd.memberId,
          cmd.recurringDetailReference,
          fromTokenRegistrationStatus(cmd.adyenTokenStatus)
        )
      )
    } else {
      apply(
        AdyenAccountUpdatedEvent(
          cmd.memberId,
          cmd.recurringDetailReference,
          fromTokenRegistrationStatus(cmd.adyenTokenStatus)
        )
      )
    }
  }

  @CommandHandler
  fun cmd(cmd: ChargeCompletedCommand) {
    val transaction =
      getSingleTransaction(
        transactions,
        cmd.transactionId,
        id
      )
    if (transaction.amount != cmd.amount) {
      log.error(
        "CRITICAL: Transaction amounts differ for transactionId: ${transaction.transactionId} " +
          "- our amount: ${transaction.amount}, " +
          "amount from payment provider: ${cmd.amount}"
      )
      apply(
        ChargeErroredEvent(
          cmd.memberId,
          cmd.transactionId,
          cmd.amount,
          "Transaction amounts differ (expected ${transaction.amount} but was ${cmd.amount})",
          cmd.timestamp
        )
      )
      throw RuntimeException("Transaction amount mismatch")
    }
    apply(
      ChargeCompletedEvent(
        id, cmd.transactionId, cmd.amount, cmd.timestamp
      )
    )
  }

  @CommandHandler
  fun cmd(cmd: ChargeFailedCommand) {
    getSingleTransaction(
      transactions,
      cmd.transactionId,
      id
    )
    apply(
      ChargeFailedEvent(
        id,
        cmd.transactionId
      )
    )
  }

  @CommandHandler
  fun cmd(cmd: PayoutCompletedCommand) {
    val transaction =
      getSingleTransaction(
        transactions,
        cmd.transactionId,
        id
      )
    if (transaction.amount != cmd.amount) {
      log.error(
        "CRITICAL: Transaction amounts differ for transactionId: ${transaction.transactionId} " +
          "- our amount: ${transaction.amount}, " +
          "amount from payment provider: ${cmd.amount}"
      )
      apply(
        PayoutErroredEvent(
          cmd.memberId,
          cmd.transactionId,
          cmd.amount,
          "Transaction amounts differ (expected ${transaction.amount} but was ${cmd.amount})",
          cmd.timestamp
        )
      )
      throw RuntimeException("Transaction amount mismatch")
    }
    apply(
      PayoutCompletedEvent(
        id,
        cmd.transactionId,
        cmd.timestamp
      )
    )
  }

  @CommandHandler
  fun cmd(cmd: PayoutFailedCommand) {
    apply(
      PayoutFailedEvent(
        id,
        cmd.transactionId,
        cmd.amount,
        cmd.timestamp
      )
    )
  }

  @EventSourcingHandler
  fun on(e: MemberCreatedEvent) {
    id = e.memberId
  }

  @EventSourcingHandler
  fun on(e: ChargeCreatedEvent) {
    val tx =
      Transaction(
        e.transactionId,
        e.amount,
        e.timestamp
      )
    tx.transactionType = TransactionType.CHARGE
    tx.transactionStatus = TransactionStatus.INITIATED
    transactions.add(tx)
  }

  @EventSourcingHandler
  fun on(e: PayoutCreatedEvent) {
    val tx =
      Transaction(
        e.transactionId,
        e.amount,
        e.timestamp
      )
    tx.transactionType = TransactionType.PAYOUT
    tx.transactionStatus = TransactionStatus.INITIATED
    transactions.add(tx)
  }

  @EventSourcingHandler
  fun on(e: ChargeCompletedEvent) {
    val tx =
      getSingleTransaction(
        transactions,
        e.transactionId,
        id
      )
    tx.transactionStatus = TransactionStatus.COMPLETED
  }

  @EventSourcingHandler
  fun on(e: ChargeFailedEvent) {
    val tx =
      getSingleTransaction(
        transactions,
        e.transactionId,
        id
      )
    tx.transactionStatus = TransactionStatus.FAILED
  }

  @EventSourcingHandler
  fun on(e: PayoutCompletedEvent) {
    val tx =
      getSingleTransaction(
        transactions,
        e.transactionId,
        id
      )
    tx.transactionStatus = TransactionStatus.COMPLETED
  }

  @EventSourcingHandler
  fun on(e: PayoutFailedEvent) {
    val transaction =
      getSingleTransaction(
        transactions,
        e.transactionId,
        id
      )
    transaction.transactionStatus = TransactionStatus.FAILED
  }

  @EventSourcingHandler
  fun on(e: TrustlyAccountCreatedEvent) {
    latestTrustlyAccountId = e.trustlyAccountId
    trustlyAccounts[e.trustlyAccountId] = null
  }

  @EventSourcingHandler
  fun on(e: DirectDebitConnectedEvent) {
    trustlyAccounts[e.trustlyAccountId] = DirectDebitStatus.CONNECTED
  }

  @EventSourcingHandler
  fun on(e: DirectDebitDisconnectedEvent) {
    trustlyAccounts[e.trustlyAccountId] = DirectDebitStatus.DISCONNECTED
  }


  @EventSourcingHandler
  fun on(e: DirectDebitPendingConnectionEvent) {
    trustlyAccounts[e.trustlyAccountId] = DirectDebitStatus.PENDING
  }

  @EventSourcingHandler
  fun on(e: AdyenAccountCreatedEvent) {
    adyenAccount = AdyenAccount(
      e.recurringDetailReference,
      e.accountStatus
    )
  }

  @EventSourcingHandler
  fun on(e: AdyenAccountUpdatedEvent) {
    adyenAccount = AdyenAccount(
      e.recurringDetailReference,
      e.accountStatus
    )
  }

  private fun updateDirectDebitStatus(cmd: UpdateTrustlyAccountCommand) {
    if (cmd.directDebitMandateActive != null && cmd.directDebitMandateActive) {
      apply(
        DirectDebitConnectedEvent(
          id,
          cmd.hedvigOrderId.toString(),
          cmd.accountId
        )
      )
    } else if (cmd.directDebitMandateActive != null && !cmd.directDebitMandateActive) {
      apply(
        DirectDebitDisconnectedEvent(
          id,
          cmd.hedvigOrderId.toString(),
          cmd.accountId
        )
      )
    } else {
      val latestDirectDebitStatus = trustlyAccounts[latestTrustlyAccountId]
      if (latestDirectDebitStatus == null || (
          latestDirectDebitStatus != DirectDebitStatus.CONNECTED
            || latestDirectDebitStatus != DirectDebitStatus.DISCONNECTED
          )
      ) {
        apply(
          DirectDebitPendingConnectionEvent(
            id,
            cmd.hedvigOrderId.toString(),
            cmd.accountId
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

  companion object {
    private fun getSingleTransaction(
      transactions: List<Transaction>,
      transactionId: UUID,
      memberId: String
    ): Transaction {
      val matchingTransactions = transactions
        .stream()
        .filter { t: Transaction -> t.transactionId == transactionId }
        .collect(Collectors.toList())
      if (matchingTransactions.size != 1) {
        throw RuntimeException(
          String.format(
            "Unexpected number of matching transactions: %n, with transactionId: %s for memberId: %s",
            matchingTransactions.size,
            transactionId.toString(),
            memberId
          )
        )
      }
      return matchingTransactions[0]
    }

    val log = LoggerFactory.getLogger(this::class.java)!!
  }
}
