package com.hedvig.paymentservice.services.trustly;

import com.google.gson.Gson;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.commons.Currency;
import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyAPIException;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.CancelNotificationData;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.CreditData;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.PendingNotificationData;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.requestbuilders.AccountPayout;
import com.hedvig.paymentService.trustly.requestbuilders.Charge;
import com.hedvig.paymentService.trustly.requestbuilders.SelectAccount;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CancelNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreditNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentErrorReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PayoutErrorReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PayoutResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PendingNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrder;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository;
import com.hedvig.paymentservice.services.Helpers;
import com.hedvig.paymentservice.services.exceptions.OrderNotFoundException;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation;
import com.hedvig.paymentservice.services.trustly.dto.PaymentRequest;
import com.hedvig.paymentservice.services.trustly.dto.PayoutRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyContextBuilder;
import javax.money.CurrencyUnit;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.javamoney.moneta.CurrencyUnitBuilder;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TrustlyService {

  private static final DateTimeFormatter trustlyTimestampFormat = getDateTimeFormatter();

  static DateTimeFormatter getDateTimeFormatter() {
    return new DateTimeFormatterBuilder()
        .appendPattern("uuuu-MM-dd HH:mm:ss")
        .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
        .appendOffset("+HH", "+00")
        .toFormatter();
  }

  private final String notificationUrl;
  public static final String COUNTRY = "SE";
  private final Logger log = LoggerFactory.getLogger(TrustlyService.class);
  private final SignedAPI api;
  private final CommandGateway gateway;
  private final UUIDGenerator uuidGenerator;
  private final String successUrl;
  private final String failUrl;

  private final TrustlyOrderRepository orderRepository;

  private final Environment springEnvironment;

  public TrustlyService(
      SignedAPI api,
      CommandGateway gateway,
      UUIDGenerator uuidGenerator,
      TrustlyOrderRepository orderRepository,
      @Value("${hedvig.trustly.successURL}") String successUrl,
      @Value("${hedvig.trustly.failURL}") String failUrl,
      @Value("${hedvig.trustly.notificationURL}") String notificationUrl,
      Environment springEnvironment) {
    this.api = api;
    this.gateway = gateway;
    this.uuidGenerator = uuidGenerator;
    this.orderRepository = orderRepository;
    this.successUrl = successUrl;
    this.failUrl = failUrl;
    this.notificationUrl = notificationUrl;
    this.springEnvironment = springEnvironment;
  }

  public DirectDebitResponse requestDirectDebitAccount(DirectDebitRequest request) {

    final UUID requestId = uuidGenerator.generateRandom();

    gateway.sendAndWait(new CreateOrderCommand(request.getMemberId(), requestId));

    return startTrustlyOrder(request.getMemberId(), request, requestId);
  }

  public void startPaymentOrder(PaymentRequest request, UUID hedvigOrderId) {
    try {

      val trustlyRequest = createPaymentRequest(hedvigOrderId, request);
      val response = api.sendRequest(trustlyRequest);

      if (response.successfulResult()) {
        val data = response.getResult().getData();
        val orderId = (String) data.get("orderid");
        log.info(
            "Payment Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
            orderId,
            hedvigOrderId);

        gateway.sendAndWait(
            new PaymentResponseReceivedCommand(hedvigOrderId, (String) data.get("url"), orderId));
      } else {
        val error = response.getError();
        log.error(
            "Paymen order creation failed: {}, {}, {}",
            error.getName(),
            error.getCode(),
            error.getMessage());
        gateway.sendAndWait(new PaymentErrorReceivedCommand(hedvigOrderId, error));
        throw new RuntimeException("Got error from trustly");
      }
    } catch (TrustlyAPIException ex) {
      throw new RuntimeException("Failed calling trustly.", ex);
    }
  }

  public void startPayoutOrder(PayoutRequest request, UUID hedvigOrderId) {
    try {
      val trustlyRequest = createPayoutRequest(hedvigOrderId, request);
      val response = api.sendRequest(trustlyRequest);

      if (response.successfulResult()) {
        val data = response.getResult().getData();

        val orderId = (String) data.get("orderid");
        log.info(
            "Payout order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
            orderId,
            hedvigOrderId);

        gateway.sendAndWait(
            new PayoutResponseReceivedCommand(hedvigOrderId, orderId, request.getAmount()));
      } else {
        val error = response.getError();
        log.error(
            "Payout order creation failed: {} {}, {}",
            error.getName(),
            error.getCode(),
            error.getMessage());
        gateway.sendAndWait(new PayoutErrorReceivedCommand(hedvigOrderId, error));
        throw new RuntimeException("Got error from trustly");
      }
    } catch (TrustlyAPIException ex) {
      throw new RuntimeException("Failed calling trustly.", ex);
    }
  }

  private DirectDebitResponse startTrustlyOrder(
      String memberId, DirectDebitRequest request, UUID requestId) {
    try {
      final Request trustlyRequest = createRequest(requestId, memberId, request);
      final Response response = api.sendRequest(trustlyRequest);

      if (response.successfulResult()) {
        final Map<String, Object> data;
        data = response.getResult().getData();
        log.info(
            "SelectAccount Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}",
            data.get("orderid"),
            requestId);

        gateway.sendAndWait(
            new SelectAccountResponseReceivedCommand(
                requestId, (String) data.get("url"), (String) data.get("orderid")));

        return new DirectDebitResponse((String) data.get("url"), requestId.toString());
      } else {
        final Error error = response.getError();
        log.info(
            "Order creation failed: {}, {}, {}",
            error.getName(),
            error.getCode(),
            error.getMessage());
        throw new RuntimeException("Got error from trustly");
      }
    } catch (TrustlyAPIException ex) {
      // gateway.sendAndWait(new SelectAccountRequestFailedCommand(requestId, ex.getMessage()));
      throw new RuntimeException("Failed calling trustly.", ex);
    }
  }

  private Request createPaymentRequest(UUID hedvigOrderId, PaymentRequest request) {
    val formatter = new DecimalFormat("#0.00");
    val amount = formatter.format(request.getAmount().getNumber().doubleValueExact());
    val build =
        new Charge.Build(
            request.getAccountId(),
            notificationUrl,
            request.getMemberId(),
            hedvigOrderId.toString(),
            amount,
            currencyUnitToTrustlyCurrency(request.getAmount().getCurrency()),
            "Hedvig månadsavgift", // TODO Better copy
            request.getEmail());

    val ret = build.getRequest();

    if (springEnvironment.acceptsProfiles("development")) {
      ret.getParams().getData().getAttributes().put("HoldNotifications", "1");
    }

    return ret;
  }

  private String createMemberEmail(String memberId) {
    return Helpers.createTrustlyInboxfromMemberId(memberId);
  }

  private Request createPayoutRequest(UUID hedvigOrderId, PayoutRequest request) {
    val formatter = new DecimalFormat("#0.00");
    val amount = formatter.format(request.getAmount().getNumber().doubleValueExact());
    val dateOfBirth = request.getDateOfBirth().format(DateTimeFormatter.ofPattern("uuuu-MM-dd"));
    val build =
        new AccountPayout.Build(
            request.getAccountId(),
            notificationUrl,
            request.getMemberId(),
            hedvigOrderId.toString(),
            amount,
            currencyUnitToTrustlyCurrency(request.getAmount().getCurrency()),
            request.getAddress(),
            request.getCountryCode(),
            dateOfBirth,
            request.getFirstName(),
            request.getLastName(),
            "PERSON",
            "Hedvig");

    val ret = build.getRequest();

    if (springEnvironment.acceptsProfiles("development")) {
      ret.getParams().getData().getAttributes().put("HoldNotifications", 1);
    }

    return ret;
  }

  private Currency currencyUnitToTrustlyCurrency(CurrencyUnit unit) {
    switch (unit.getCurrencyCode()) {
      case "SEK":
        return Currency.SEK;
      default:
        throw new RuntimeException("Unsupported currency type");
    }
  }

  private Request createRequest(UUID hedvigOrderId, String memberId, DirectDebitRequest request) {
    final SelectAccount.Build build =
        new SelectAccount.Build(notificationUrl, memberId, hedvigOrderId.toString());
    build.requestDirectDebitMandate("1");
    build.firstName(request.getFirstName());
    build.lastName(request.getLastName());
    build.country(COUNTRY);
    build.email(createMemberEmail(request.getMemberId()));
    build.locale("sv_SE");
    build.nationalIdentificationNumber(request.getSsn());
    build.successURL(appendTriggerId(successUrl, request.getTriggerId()));
    build.failURL(appendTriggerId(failUrl, request.getTriggerId()));

    final Request request1 = build.getRequest();
    final Gson gson = new Gson();
    log.info("Trustly request details: {}", gson.toJson(request1));

    if (springEnvironment.acceptsProfiles("development")) {
      request1.getParams().getData().getAttributes().put("HoldNotifications", "1");
    }

    return request1;
  }

  private String appendTriggerId(String failUrl, String triggerId) {
    return failUrl + "&triggerId=" + triggerId;
  }

  public Response sendRequest(Request request) {
    return api.sendRequest(request);
  }

  public ResponseStatus recieveNotification(Notification notification) {

    log.info("Received notification from Trustly: {}", notification.getMethod());

    UUID requestId = UUID.fromString(notification.getParams().getData().getMessageId());

    switch (notification.getMethod()) {
      case ACCOUNT:
        val accountData = (AccountNotificationData) notification.getParams().getData();

        val accountAttributes = accountData.getAttributes();
        val directDebitMandate = (String) accountAttributes.getOrDefault("directdebitmandate", "0");
        val lastDigits = (String) accountAttributes.get("lastdigits");
        val clearingHouse = (String) accountAttributes.get("clearinghouse");
        val bank = (String) accountAttributes.get("bank");
        val descriptor = (String) accountAttributes.get("descriptor");
        val personId = (String) accountAttributes.get("personid");
        val name = (String) accountAttributes.get("name");
        val address = (String) accountAttributes.get("address");
        val zipCode = (String) accountAttributes.get("zipcode");
        val city = (String) accountAttributes.get("city");

        val accountId = accountData.getAccountId();
        val notificationId = accountData.getNotificationId();
        val orderId = accountData.getOrderId();

        gateway.sendAndWait(
            new AccountNotificationReceivedCommand(
                requestId,
                notificationId,
                orderId,
                accountId,
                address,
                bank,
                city,
                clearingHouse,
                descriptor,
                directDebitMandate.equals("1"),
                lastDigits,
                name,
                personId,
                zipCode));
        break;

      case PENDING:
        val pendingData = (PendingNotificationData) notification.getParams().getData();

        val pendingCurrency = trustlyCurrencyToCurrencyUnit(pendingData.getCurrency());
        val pendingAmount = Money.of(new BigDecimal(pendingData.getAmount()), pendingCurrency);

        val pendingTimestamp =
            OffsetDateTime.parse(pendingData.getTimestamp(), trustlyTimestampFormat).toInstant();

        gateway.sendAndWait(
            new PendingNotificationReceivedCommand(
                requestId,
                pendingData.getNotificationId(),
                pendingData.getOrderId(),
                pendingAmount,
                pendingData.getEndUserId(),
                pendingTimestamp));
        break;

      case CREDIT:
        val creditData = (CreditData) notification.getParams().getData();

        val creditTimestamp =
            OffsetDateTime.parse(creditData.getTimestamp(), trustlyTimestampFormat).toInstant();
        val creditedCurrency = trustlyCurrencyToCurrencyUnit(creditData.getCurrency());

        val creditedAmount = Money.of(new BigDecimal(creditData.getAmount()), creditedCurrency);

        gateway.sendAndWait(
            new CreditNotificationReceivedCommand(
                requestId,
                creditData.getNotificationId(),
                creditData.getOrderId(),
                creditData.getEndUserId(),
                creditedAmount,
                creditTimestamp));

        break;

      case CANCEL:
        val cancelData = (CancelNotificationData) notification.getParams().getData();

        gateway.sendAndWait(
            new CancelNotificationReceivedCommand(
                requestId,
                cancelData.getNotificationId(),
                cancelData.getOrderId(),
                cancelData.getEndUserId()));
        break;

      default:
        throw new RuntimeException(
            String.format(
                "Cannot handle notification type: %s", notification.getMethod().toString()));
    }

    return ResponseStatus.OK;
  }

  private CurrencyUnit trustlyCurrencyToCurrencyUnit(Currency c) {
    val currencyContext = CurrencyContextBuilder.of("TrustlyService").build();
    switch (c) {
      case SEK:
        return CurrencyUnitBuilder.of("SEK", currencyContext).build();
      default:
        throw new RuntimeException(
            String.format("Cannot handle currency of type: %s", c.toString()));
    }
  }

  public OrderInformation orderInformation(UUID requestId) {

    final Optional<TrustlyOrder> byId = this.orderRepository.findById(requestId);
    final TrustlyOrder trustlyOrder =
        byId.orElseThrow(() -> new OrderNotFoundException("Order not found with id " + requestId));

    return new OrderInformation(requestId, trustlyOrder.getIframeUrl(), trustlyOrder.getState());
  }
}
