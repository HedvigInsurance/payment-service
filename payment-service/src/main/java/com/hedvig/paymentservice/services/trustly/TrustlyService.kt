package com.hedvig.paymentservice.services.trustly

import com.google.gson.Gson
import com.hedvig.paymentService.trustly.SignedAPI
import com.hedvig.paymentService.trustly.commons.Currency
import com.hedvig.paymentService.trustly.commons.Method
import com.hedvig.paymentService.trustly.commons.ResponseStatus
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyAPIException
import com.hedvig.paymentService.trustly.data.notification.Notification
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData
import com.hedvig.paymentService.trustly.data.notification.notificationdata.CancelNotificationData
import com.hedvig.paymentService.trustly.data.notification.notificationdata.CreditData
import com.hedvig.paymentService.trustly.data.notification.notificationdata.PendingNotificationData
import com.hedvig.paymentService.trustly.data.request.Request
import com.hedvig.paymentService.trustly.data.response.Response
import com.hedvig.paymentService.trustly.requestbuilders.AccountPayout
import com.hedvig.paymentService.trustly.requestbuilders.Charge
import com.hedvig.paymentService.trustly.requestbuilders.SelectAccount
import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.configuration.HedvigTrustlyConfiguration
import com.hedvig.paymentservice.domain.accountRegistration.commands.CreateAccountRegistrationRequestCommand
import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationCancellationCommand
import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.trustlyOrder.commands.*
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository
import com.hedvig.paymentservice.services.Helpers
import com.hedvig.paymentservice.services.exceptions.OrderNotFoundException
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation
import com.hedvig.paymentservice.services.trustly.dto.PaymentRequest
import com.hedvig.paymentservice.services.trustly.dto.PayoutRequest
import com.hedvig.paymentservice.services.trustly.exceptions.InvalidRedirectException
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse
import org.axonframework.commandhandling.gateway.CommandGateway
import org.javamoney.moneta.CurrencyUnitBuilder
import org.javamoney.moneta.Money
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URI
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import javax.money.CurrencyContextBuilder
import javax.money.CurrencyUnit

@Component
class TrustlyService(
  private val api: SignedAPI,
  private val gateway: CommandGateway,
  private val uuidGenerator: UUIDGenerator,
  private val orderRepository: TrustlyOrderRepository,
  private val accountRegistrationRepository: AccountRegistrationRepository,
  private val trustlyConfiguration: HedvigTrustlyConfiguration,
  @param:Value("\${hedvig.trustly.successURL}") private val redirectingToBotServiceSuccessUrl: String,
  @param:Value("\${hedvig.trustly.failURL}") private val redirectingToBotServiceFailUrl: String,
  @param:Value("\${hedvig.trustly.notificationURL}") private val notificationUrl: String,
  @param:Value("\${hedvig.trustly.non.redirecting.to.botService.successURL}") private val plainSuccessUrl: String,
  @param:Value("\${hedvig.trustly.non.redirecting.to.botService.failURL}") private val plainFailUrl: String,
  @param:Value("\${hedvig.trustly.use.claims.account}") private val useClaimsAccount: Boolean,
  private val springEnvironment: Environment
) {
  private val log = LoggerFactory.getLogger(TrustlyService::class.java)

  fun requestDirectDebitAccount(
    info: DirectDebitOrderInfo,
    clientSuccessUrl: String? = null,
    clientFailureUrl: String? = null
  ): DirectDebitResponse {

    val hedvigOrderId = uuidGenerator.generateRandom()

    try {
      val trustlyRequest =
        createRequest(info, hedvigOrderId, clientSuccessUrl = clientSuccessUrl, clientFailureUrl = clientFailureUrl)
      val response = api.sendRequest(trustlyRequest, false)

      if (response.successfulResult()) {
        val data: Map<String, Any> = response.result.data

        val trustlyUrl: String = data["url"] as String
        val trustlyOrderId: String = data["orderid"] as String

        log.info(
          "SelectAccount Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
          trustlyOrderId,
          hedvigOrderId
        )

        gateway.sendAndWait<Any>(
          CreateAccountRegistrationRequestCommand(
            uuidGenerator.generateRandom(),
            hedvigOrderId,
            info.memberId,
            trustlyOrderId,
            trustlyUrl
          )
        )

        return DirectDebitResponse(trustlyUrl, hedvigOrderId.toString())
      } else {
        val error = response.error
        log.info(
          "Order creation failed: {}, {}, {}",
          error.name,
          error.code,
          error.message
        )
        throw RuntimeException("Got error from trustly")
      }
    } catch (ex: TrustlyAPIException) {
      // gateway.sendAndWait(new SelectAccountRequestFailedCommand(requestId, ex.getMessage()));
      throw RuntimeException("Failed calling trustly.", ex)
    }
  }

  fun cancelDirectDebitAccountRequest(memberId: String): Boolean {
    val stream: List<AccountRegistration> = accountRegistrationRepository.findByMemberId(memberId)
      .filter { x ->
        x.status != AccountRegistrationStatus.CANCELLED
          && x.status != AccountRegistrationStatus.CONFIRMED
          && x.status != AccountRegistrationStatus.IN_PROGRESS
      }

    when {
      stream.count() < 1 -> return false
      stream.count() == 1 -> {
        val accountRegistration: AccountRegistration = stream.single()
        gateway.sendAndWait<Any>(
          ReceiveAccountRegistrationCancellationCommand(
            accountRegistration.accountRegistrationId,
            accountRegistration.hedvigOrderId,
            accountRegistration.memberId
          )
        )
        return true
      }
      else -> {
        val optionalAccountRegistration: AccountRegistration? = stream.maxBy { x -> x.initiated }
        if (optionalAccountRegistration != null) {
          gateway.sendAndWait<Any>(
            ReceiveAccountRegistrationCancellationCommand(
              optionalAccountRegistration.accountRegistrationId,
              optionalAccountRegistration.hedvigOrderId,
              optionalAccountRegistration.memberId
            )
          )
        }
        return true
      }
    }
  }

  fun startPaymentOrder(request: PaymentRequest, hedvigOrderId: UUID) {
    try {

      val trustlyRequest = createPaymentRequest(hedvigOrderId, request)
      val response = api.sendRequest(trustlyRequest, false)

      if (response.successfulResult()) {
        val data = response.result.data
        val orderId = data["orderid"] as String
        log.info(
          "Payment Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
          orderId,
          hedvigOrderId
        )

        gateway.sendAndWait<Any>(
          PaymentResponseReceivedCommand(hedvigOrderId, data["url"] as String?, orderId)
        )
      } else {
        val error = response.error
        log.error(
          "Paymen order creation failed: {}, {}, {}",
          error.name,
          error.code,
          error.message
        )
        gateway.sendAndWait<Any>(PaymentErrorReceivedCommand(hedvigOrderId, error))
        throw RuntimeException("Got error from trustly")
      }
    } catch (ex: TrustlyAPIException) {
      throw RuntimeException("Failed calling trustly.", ex)
    }

  }

  fun startPayoutOrder(request: PayoutRequest, hedvigOrderId: UUID) {
    try {
      val trustlyRequest = createPayoutRequest(hedvigOrderId, request)

      val response = api.sendRequest(trustlyRequest, request.category == TransactionCategory.CLAIM && useClaimsAccount)

      if (response.successfulResult()) {
        val data = response.result.data

        val orderId = data["orderid"] as String
        log.info(
          "Payout order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
          orderId,
          hedvigOrderId
        )

        gateway.sendAndWait<Any>(
          PayoutResponseReceivedCommand(hedvigOrderId, orderId, request.amount)
        )
      } else {
        val error = response.error
        log.error(
          "Payout order creation failed: {} {}, {}",
          error.name,
          error.code,
          error.message
        )
        gateway.sendAndWait<Any>(PayoutErrorReceivedCommand(hedvigOrderId, error))
        throw RuntimeException("Got error from trustly")
      }
    } catch (ex: TrustlyAPIException) {
      throw RuntimeException("Failed calling trustly.", ex)
    }

  }

  private fun createPaymentRequest(hedvigOrderId: UUID, request: PaymentRequest): Request {
    val formatter = DecimalFormat("#0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val amount = formatter.format(request.amount.number.doubleValueExact())
    val build = Charge.Build(
      request.accountId,
      notificationUrl,
      request.memberId,
      hedvigOrderId.toString(),
      amount,
      currencyUnitToTrustlyCurrency(request.amount.currency),
      "Hedvig månadsavgift", // TODO Better copy
      request.email
    )

    val ret = build.request

    if (springEnvironment.acceptsProfiles("development")) {
      ret.params.data.attributes["HoldNotifications"] = "1"
    }

    return ret
  }

  private fun createMemberEmail(memberId: String): String {
    return Helpers.createTrustlyInboxfromMemberId(memberId)
  }

  private fun createPayoutRequest(hedvigOrderId: UUID, request: PayoutRequest): Request {
    val formatter = DecimalFormat("#0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val amount = formatter.format(request.amount.number.doubleValueExact())
    val dateOfBirth = request.dateOfBirth.format(DateTimeFormatter.ofPattern("uuuu-MM-dd"))
    val build = AccountPayout.Build(
      request.accountId,
      notificationUrl,
      request.memberId,
      hedvigOrderId.toString(),
      amount,
      currencyUnitToTrustlyCurrency(request.amount.currency),
      request.address,
      request.countryCode,
      dateOfBirth,
      request.firstName,
      request.lastName,
      "PERSON",
      "Hedvig"
    )

    val ret = build.request

    if (springEnvironment.acceptsProfiles("development")) {
      ret.params.data.attributes["HoldNotifications"] = 1
    }

    return ret
  }

  private fun currencyUnitToTrustlyCurrency(unit: CurrencyUnit): Currency {
    when (unit.currencyCode) {
      "SEK" -> return Currency.SEK
      else -> throw RuntimeException("Unsupported currency type")
    }
  }

  private fun createRequest(
    info: DirectDebitOrderInfo,
    hedvigOrderId: UUID,
    clientSuccessUrl: String?,
    clientFailureUrl: String?
  ): Request {
    val build = SelectAccount.Build(notificationUrl, info.memberId, hedvigOrderId.toString())
    build.requestDirectDebitMandate("1")
    build.firstName(info.firstName)
    build.lastName(info.lastName)
    build.country(COUNTRY)
    build.email(createMemberEmail(info.memberId))
    build.locale("sv_SE")
    build.nationalIdentificationNumber(info.personalNumber)
    val successUrl = when {
      info.redirectingToBotService -> appendTriggerId(redirectingToBotServiceSuccessUrl, info.triggerId)
      clientSuccessUrl != null -> requireValidRedirect(clientSuccessUrl)
      else -> plainSuccessUrl
    }
    val failUrl = when {
      info.redirectingToBotService -> appendTriggerId(redirectingToBotServiceFailUrl, info.triggerId)
      clientFailureUrl != null -> requireValidRedirect(clientFailureUrl)
      else -> plainFailUrl
    }
    build.successURL(successUrl)
    build.failURL(failUrl)
    build.URLTarget("_self")

    val directDebitOrderRequest = build.request
    val gson = Gson()
    log.info("Trustly request details: {}", gson.toJson(directDebitOrderRequest))

    if (springEnvironment.acceptsProfiles("development")) {
      directDebitOrderRequest.params.data.attributes["HoldNotifications"] = "1"
    }

    return directDebitOrderRequest
  }

  private fun requireValidRedirect(url: String): String {
    try {
      val uri = URI.create(url)
      if (
        !trustlyConfiguration.validRedirectHosts.contains(uri.host)
        && !trustlyConfiguration.validRedirectHosts.contains("${uri.host}:${uri.port}")
      ) {
        throw InvalidRedirectException("Host \"${uri.host}\" is not a whitelisted redirect")
      }
      return url
    } catch (e: IllegalArgumentException) {
      throw InvalidRedirectException("Unable to parse host of URL \"$url\"", e)
    }
  }

  private fun appendTriggerId(failUrl: String, triggerId: String?): String {
    return "$failUrl&triggerId=$triggerId"
  }

  fun sendRequest(request: Request): Response {
    return api.sendRequest(request, false)
  }

  fun receiveNotification(notification: Notification): ResponseStatus {

    log.info("Received notification from Trustly: {}", notification.method)

    val requestId = UUID.fromString(notification.params.data.messageId)

    when (notification.method) {
      Method.ACCOUNT -> {
        val accountData = notification.params.data as AccountNotificationData

        val accountAttributes = accountData.attributes
        val directDebitMandate = accountAttributes["directdebitmandate"] as String?
        val lastDigits = accountAttributes["lastdigits"] as String?
        val clearingHouse = accountAttributes["clearinghouse"] as String?
        val bank = accountAttributes["bank"] as String?
        val descriptor = accountAttributes["descriptor"] as String?
        val personId = accountAttributes["personid"] as String?
        val name = accountAttributes["name"] as String?
        val address = accountAttributes["address"] as String?
        val zipCode = accountAttributes["zipcode"] as String?
        val city = accountAttributes["city"] as String?

        val accountId = accountData.accountId
        val notificationId = accountData.notificationId
        val orderId = accountData.orderId

        gateway.sendAndWait<Any>(
          AccountNotificationReceivedCommand(
            requestId,
            notificationId,
            orderId!!,
            accountId,
            address,
            bank,
            city,
            clearingHouse,
            descriptor,
            directDebitMandate?.equals("1"),
            lastDigits,
            name,
            personId,
            zipCode
          )
        )
      }

      Method.PENDING -> {
        val pendingData = notification.params.data as PendingNotificationData

        val pendingCurrency = trustlyCurrencyToCurrencyUnit(pendingData.currency)
        val pendingAmount = Money.of(BigDecimal(pendingData.amount), pendingCurrency)

        val pendingTimestamp = OffsetDateTime.parse(pendingData.timestamp, trustlyTimestampFormat).toInstant()

        gateway.sendAndWait<Any>(
          PendingNotificationReceivedCommand(
            requestId,
            pendingData.notificationId,
            pendingData.orderId,
            pendingAmount,
            pendingData.endUserId,
            pendingTimestamp
          )
        )
      }

      Method.CREDIT -> {
        val creditData = notification.params.data as CreditData

        val creditTimestamp = OffsetDateTime.parse(creditData.timestamp, trustlyTimestampFormat).toInstant()
        val creditedCurrency = trustlyCurrencyToCurrencyUnit(creditData.currency)

        val creditedAmount = Money.of(BigDecimal(creditData.amount), creditedCurrency)

        gateway.sendAndWait<Any>(
          CreditNotificationReceivedCommand(
            requestId,
            creditData.notificationId,
            creditData.orderId,
            creditData.endUserId,
            creditedAmount,
            creditTimestamp
          )
        )
      }

      Method.CANCEL -> {
        val cancelData = notification.params.data as CancelNotificationData

        gateway.sendAndWait<Any>(
          CancelNotificationReceivedCommand(
            requestId,
            cancelData.notificationId,
            cancelData.orderId,
            cancelData.endUserId
          )
        )
      }

      else -> throw RuntimeException(
        String.format(
          "Cannot handle notification type: %s", notification.method.toString()
        )
      )
    }

    return ResponseStatus.OK
  }

  private fun trustlyCurrencyToCurrencyUnit(c: Currency): CurrencyUnit {
    val currencyContext = CurrencyContextBuilder.of("TrustlyService").build()
    when (c) {
      Currency.SEK -> return CurrencyUnitBuilder.of("SEK", currencyContext).build()
      else -> throw RuntimeException(
        String.format("Cannot handle currency of type: %s", c.toString())
      )
    }
  }

  fun orderInformation(requestId: UUID): OrderInformation {

    val byId = this.orderRepository.findById(requestId)
    val trustlyOrder = byId.orElseThrow { OrderNotFoundException("Order not found with id $requestId") }

    return OrderInformation(requestId, trustlyOrder.iframeUrl, trustlyOrder.state)
  }

  companion object {

    private val trustlyTimestampFormat = dateTimeFormatter

    val dateTimeFormatter: DateTimeFormatter
      get() = DateTimeFormatterBuilder()
        .appendPattern("uuuu-MM-dd HH:mm:ss")
        .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
        .appendOffset("+HH", "+00")
        .toFormatter()
    val COUNTRY = "SE"
  }
}
