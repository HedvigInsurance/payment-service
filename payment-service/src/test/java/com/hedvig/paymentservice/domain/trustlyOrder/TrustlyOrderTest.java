package com.hedvig.paymentservice.domain.trustlyOrder;

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.HEDVIG_ORDER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_CITY;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_SSN;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_STREET;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_ZIP;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVAN_FIRST_NAME;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_BANK;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_DESCRIPTOR;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_LAST_DIGITS;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_NOTIFICATION_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ORDER_ID;

import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.NotificationParameters;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.AccountNotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.AccountNotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.NotificationReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderAssignedTrustlyIdEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.SelectAccountResponseReceivedEvent;
import com.hedvig.paymentservice.trustly.testHelpers.TestData;
import java.util.HashMap;
import java.util.UUID;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyOrderTest {

  private FixtureConfiguration<TrustlyOrder> fixture;

  @Before
  public void setUp() {
    fixture = new AggregateTestFixture<>(TrustlyOrder.class);
  }

  @Test
  public void selectAccountReceivedCommandTriggersTwoEvents() {

    fixture
        .given(orderCreatedEvent())
        .when(selectAccountCommand())
        .expectSuccessfulHandlerExecution()
        .expectEvents(orderAssignedTrustlyIdEvent(), selectAccountResponseReceivedEvent());
  }

  @Test
  public void
      GIVEN_trustlyOrder_WHEN_accountNotification_THEN_notificationReceived_AND_accountNotificationReceived_AND_orderCompletedEvents() {
    fixture
        .given(orderCreatedEvent(), orderAssignedTrustlyIdEvent())
        .when(
            makeAccountNotificationReceivedCommand(
                TRUSTLY_NOTIFICATION_ID, TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            // notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID),
            accountNotificationRecievedEvent(
                TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE, TRUSTLY_NOTIFICATION_ID),
            orderCompletedEvent());
  }

  public AccountNotificationReceivedCommand makeAccountNotificationReceivedCommand(
      String trustlyNotificationId, boolean trustlyAccountDirectdebitFalse) {
    return new AccountNotificationReceivedCommand(
        HEDVIG_ORDER_ID,
        trustlyNotificationId,
        TRUSTLY_ORDER_ID,
        TRUSTLY_ACCOUNT_ID,
        TOLVANSSON_STREET,
        TRUSTLY_ACCOUNT_BANK,
        TOLVANSSON_CITY,
        TRUSTLY_ACCOUNT_CLEARING_HOUSE,
        TRUSTLY_ACCOUNT_DESCRIPTOR,
        trustlyAccountDirectdebitFalse,
        TRUSTLY_ACCOUNT_LAST_DIGITS,
        TOLVAN_FIRST_NAME,
        TOLVANSSON_SSN,
        TOLVANSSON_ZIP);
  }

  @Test
  public void
      GIVEN_trustlyOrderWithAccountReceivedEvent_WHEN_accountNotificationTHENsendOnlyAccountEvents() {
    final String notificationId = "872943";

    fixture
        .given(
            orderCreatedEvent(),
            orderAssignedTrustlyIdEvent(),
            notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID),
            accountNotificationRecievedEvent(
                TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE, TestData.TRUSTLY_NOTIFICATION_ID),
            orderCompletedEvent())
        .when(
            makeAccountNotificationReceivedCommand(
                notificationId, TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            accountNotificationRecievedEvent(TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE, notificationId));
  }

  @Test
  public void GIVEN_oneAccountNotificaiton_WHEN_newAccountNotification_THEN_doNothing() {

    fixture
        .given(
            orderCreatedEvent(),
            orderAssignedTrustlyIdEvent(),
            notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID))
        .when(
            makeAccountNotificationReceivedCommand(
                TRUSTLY_NOTIFICATION_ID, TRUSTLY_ACCOUNT_DIRECTDEBIT_FALSE))
        .expectSuccessfulHandlerExecution()
        .expectEvents();
  }

  public OrderCompletedEvent orderCompletedEvent() {
    return new OrderCompletedEvent(TestData.HEDVIG_ORDER_ID);
  }

  public AccountNotificationReceivedEvent accountNotificationRecievedEvent(
      boolean directDebitMandate, String notificationId) {
    return new AccountNotificationReceivedEvent(
        HEDVIG_ORDER_ID,
        TOLVANSSON_MEMBER_ID,
        notificationId,
        TRUSTLY_ORDER_ID,
        TRUSTLY_ACCOUNT_ID,
        TOLVANSSON_STREET,
        TRUSTLY_ACCOUNT_BANK,
        TOLVANSSON_CITY,
        TRUSTLY_ACCOUNT_CLEARING_HOUSE,
        TRUSTLY_ACCOUNT_DESCRIPTOR,
        directDebitMandate,
        TRUSTLY_ACCOUNT_LAST_DIGITS,
        TOLVAN_FIRST_NAME,
        TOLVANSSON_SSN,
        TOLVANSSON_ZIP);
  }

  public NotificationReceivedEvent notificationReceivedEvent(
      String notificationId, String trustlyOrderId) {
    return new NotificationReceivedEvent(TestData.HEDVIG_ORDER_ID, notificationId, trustlyOrderId);
  }

  private Notification accountNotification(
      String trustlyNotificationId, Boolean directDebitMandate) {
    Notification notification = new Notification();
    NotificationParameters parameters = new NotificationParameters();
    notification.setParams(parameters);
    notification.setMethod(Method.ACCOUNT);
    parameters.setUUID(UUID.randomUUID().toString());
    AccountNotificationData data = new AccountNotificationData();
    parameters.setData(data);

    data.setOrderId(TRUSTLY_ORDER_ID);
    data.setAccountId(TestData.TRUSTLY_ACCOUNT_ID);
    data.setMessageId(TestData.HEDVIG_ORDER_ID.toString());
    data.setNotificationId(trustlyNotificationId);
    data.setVerified(true);

    final HashMap<String, Object> attributes = new HashMap<>();

    attributes.put("descriptor", TestData.TRUSTLY_ACCOUNT_DESCRIPTOR);
    attributes.put("bank", TestData.TRUSTLY_ACCOUNT_BANK);
    attributes.put("clearinghouse", TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE);
    attributes.put("lastdigits", TestData.TRUSTLY_ACCOUNT_LAST_DIGITS);
    if (directDebitMandate != null) {
      attributes.put("directdebitmandate", directDebitMandate ? "1" : "0");
    }
    data.setAttributes(attributes);

    return notification;
  }

  private SelectAccountResponseReceivedEvent selectAccountResponseReceivedEvent() {
    return new SelectAccountResponseReceivedEvent(
        TestData.HEDVIG_ORDER_ID, TestData.TRUSTLY_IFRAME_URL);
  }

  private OrderAssignedTrustlyIdEvent orderAssignedTrustlyIdEvent() {
    return new OrderAssignedTrustlyIdEvent(TestData.HEDVIG_ORDER_ID, TRUSTLY_ORDER_ID);
  }

  private SelectAccountResponseReceivedCommand selectAccountCommand() {
    return new SelectAccountResponseReceivedCommand(
        TestData.HEDVIG_ORDER_ID, TestData.TRUSTLY_IFRAME_URL, TRUSTLY_ORDER_ID);
  }

  private OrderCreatedEvent orderCreatedEvent() {
    return new OrderCreatedEvent(TestData.HEDVIG_ORDER_ID, TestData.TOLVANSSON_MEMBER_ID);
  }
}
