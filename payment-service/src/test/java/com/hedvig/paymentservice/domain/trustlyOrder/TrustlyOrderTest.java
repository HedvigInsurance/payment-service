package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.NotificationParameters;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.NotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyOrderTest {

    public static final String TRUSTLY_ORDER_ID = "12313213";
    public static final String TRUSTLY_ACCOUNT_ID = "456456";
    public static final String IFRAME_URL = "https://trustly.com/iframeurl...";
    public static final String TRUSTLY_NOTIFICATION_ID = "1381313";
    public static final String SWEDBANK = "Swedbank";
    public static final String DESCRIPTIOR = "**145678";
    public static final String SWEDEN = "SWEDEN";
    public static final String LAST_DIGITS = "145678";
    private static final String MEMBER_ID = "1337";
    private FixtureConfiguration<TrustlyOrder> fixture;
    public static final UUID HEDVIG_ORDER_ID = UUID.randomUUID();

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture<>(TrustlyOrder.class);
    }

    @Test
    public void selectAccountReceivedCommandTriggersTwoEvents() {

        fixture.given(orderCreatedEvent())
                .when(selectAccountCommand())
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        orderAssignedTrustlyIdEvent(),
                        selectAccountResponseReceivedEvent()
                        );
    }

    @Test
    public void GIVEN_trustlyOrder_WHEN_accountNotification_THEN_notificationReceived_AND_accountNotificationReceived_AND_orderCompletedEvents() {
        fixture.given(
                    orderCreatedEvent(),
                    orderAssignedTrustlyIdEvent())
                .when(
                    new NotificationReceivedCommand(HEDVIG_ORDER_ID, accountNotification(TRUSTLY_NOTIFICATION_ID, null)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                    notificationReceivedEvent(TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID),
                    accountNotificationRecievedEvent(false, TRUSTLY_NOTIFICATION_ID),
                    orderCompletedEvent()
                );
    }

    @Test
    public void GIVEN_trustlyOrderWithAccountReceivedEvent_WHEN_accountNotificationTHENsendOnlyAccountEvents() {
        final String notificationId = "872943";

        fixture.given(
                    orderCreatedEvent(),
                    orderAssignedTrustlyIdEvent(),
                    notificationReceivedEvent(TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID),
                    accountNotificationRecievedEvent(false, TRUSTLY_NOTIFICATION_ID),
                    orderCompletedEvent())
                .when(
                        new NotificationReceivedCommand(HEDVIG_ORDER_ID, accountNotification(notificationId, true)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        notificationReceivedEvent(notificationId, TRUSTLY_ORDER_ID),
                        accountNotificationRecievedEvent(true, notificationId)
                );
    }

    @Test
    public void GIVEN_oneAccountNotificaiton_WHEN_newAccountNotification_THEN_doNothing() {

        fixture
                .given(
                    orderCreatedEvent(),
                    orderAssignedTrustlyIdEvent(),
                    notificationReceivedEvent(TRUSTLY_NOTIFICATION_ID, TRUSTLY_ORDER_ID))
                .when(
                    new NotificationReceivedCommand(HEDVIG_ORDER_ID, accountNotification(TRUSTLY_NOTIFICATION_ID, false)))
                .expectSuccessfulHandlerExecution()
                .expectEvents();

    }

    public OrderCompletedEvent orderCompletedEvent() {
        return new OrderCompletedEvent(HEDVIG_ORDER_ID);
    }

    public AccountNotificationReceivedEvent accountNotificationRecievedEvent(boolean directDebitMandate, String notificationId) {
        return new AccountNotificationReceivedEvent(HEDVIG_ORDER_ID, notificationId,
                TRUSTLY_ORDER_ID,
                TRUSTLY_ACCOUNT_ID,
                null,
                SWEDBANK,
                null,
                SWEDEN,
                DESCRIPTIOR,
                directDebitMandate,
                LAST_DIGITS,
                null,
                null,
                null
        );
    }

    public NotificationReceivedEvent notificationReceivedEvent(String notificationId, String trustlyOrderId) {
        return new NotificationReceivedEvent(HEDVIG_ORDER_ID, notificationId, trustlyOrderId);
    }

    private Notification accountNotification(String trustlyNotificationId, Boolean directDebitMandate) {
        Notification notification = new Notification();
        NotificationParameters parameters = new NotificationParameters();
        notification.setParams(parameters);
        notification.setMethod(Method.ACCOUNT);
        parameters.setUUID(UUID.randomUUID().toString());
        AccountNotificationData data = new AccountNotificationData();
        parameters.setData(data);

        data.setOrderId(TRUSTLY_ORDER_ID);
        data.setAccountId(TRUSTLY_ACCOUNT_ID);
        data.setMessageId(HEDVIG_ORDER_ID.toString());
        data.setNotificationId(trustlyNotificationId);
        data.setVerified(true);

        final HashMap<String, Object> attributes = new HashMap<>();

        attributes.put("descriptor", DESCRIPTIOR);
        attributes.put("bank", SWEDBANK);
        attributes.put("clearinghouse", SWEDEN);
        attributes.put("lastdigits", LAST_DIGITS);
        if(directDebitMandate != null) {
            attributes.put("directdebitmandate", directDebitMandate ? "1" : "0");
        }
        data.setAttributes(attributes);

        return notification;
    }

    private SelectAccountResponseReceivedEvent selectAccountResponseReceivedEvent() {
        return new SelectAccountResponseReceivedEvent(HEDVIG_ORDER_ID, IFRAME_URL);
    }

    private OrderAssignedTrustlyIdEvent orderAssignedTrustlyIdEvent() {
        return new OrderAssignedTrustlyIdEvent(HEDVIG_ORDER_ID, TRUSTLY_ORDER_ID);
    }

    private SelectAccountResponseReceivedCommand selectAccountCommand() {
        return new SelectAccountResponseReceivedCommand(HEDVIG_ORDER_ID, IFRAME_URL, TRUSTLY_ORDER_ID);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(HEDVIG_ORDER_ID, MEMBER_ID);
    }
}