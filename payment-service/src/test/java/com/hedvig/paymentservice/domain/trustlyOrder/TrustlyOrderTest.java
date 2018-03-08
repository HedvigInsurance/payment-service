package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.NotificationParameters;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.NotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
import com.hedvig.paymentservice.trustly.testHelpers.TestData;
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

    private FixtureConfiguration<TrustlyOrder> fixture;

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
                    new NotificationReceivedCommand(TestData.HEDVIG_ORDER_ID, accountNotification(TestData.TRUSTLY_NOTIFICATION_ID, null)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                    notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TestData.TRUSTLY_ORDER_ID),
                    accountNotificationRecievedEvent(false, TestData.TRUSTLY_NOTIFICATION_ID),
                    orderCompletedEvent()
                );
    }

    @Test
    public void GIVEN_trustlyOrderWithAccountReceivedEvent_WHEN_accountNotificationTHENsendOnlyAccountEvents() {
        final String notificationId = "872943";

        fixture.given(
                    orderCreatedEvent(),
                    orderAssignedTrustlyIdEvent(),
                    notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TestData.TRUSTLY_ORDER_ID),
                    accountNotificationRecievedEvent(false, TestData.TRUSTLY_NOTIFICATION_ID),
                    orderCompletedEvent())
                .when(
                        new NotificationReceivedCommand(TestData.HEDVIG_ORDER_ID, accountNotification(notificationId, true)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        notificationReceivedEvent(notificationId, TestData.TRUSTLY_ORDER_ID),
                        accountNotificationRecievedEvent(true, notificationId)
                );
    }

    @Test
    public void GIVEN_oneAccountNotificaiton_WHEN_newAccountNotification_THEN_doNothing() {

        fixture
                .given(
                    orderCreatedEvent(),
                    orderAssignedTrustlyIdEvent(),
                    notificationReceivedEvent(TestData.TRUSTLY_NOTIFICATION_ID, TestData.TRUSTLY_ORDER_ID))
                .when(
                    new NotificationReceivedCommand(TestData.HEDVIG_ORDER_ID, accountNotification(TestData.TRUSTLY_NOTIFICATION_ID, false)))
                .expectSuccessfulHandlerExecution()
                .expectEvents();

    }

    public OrderCompletedEvent orderCompletedEvent() {
        return new OrderCompletedEvent(TestData.HEDVIG_ORDER_ID);
    }

    public AccountNotificationReceivedEvent accountNotificationRecievedEvent(boolean directDebitMandate, String notificationId) {
        return new AccountNotificationReceivedEvent(
                TestData.HEDVIG_ORDER_ID,
                TestData.MEMBER_ID,
                notificationId,
                TestData.TRUSTLY_ORDER_ID,
                TestData.TRUSTLY_ACCOUNT_ID,
                null,
                TestData.TRUSTLY_ACCOUNT_BANK,
                null,
                TestData.TOLVANSSON_CITY,
                TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE,
                directDebitMandate,
                TestData.TRUSTLY_ACCOUNT_DESCRIPTOR,
                TestData.TRUSTLY_ACCOUNT_LAST_DIGITS,
                null,
                null
        );
    }

    public NotificationReceivedEvent notificationReceivedEvent(String notificationId, String trustlyOrderId) {
        return new NotificationReceivedEvent(TestData.HEDVIG_ORDER_ID, notificationId, trustlyOrderId);
    }

    private Notification accountNotification(String trustlyNotificationId, Boolean directDebitMandate) {
        Notification notification = new Notification();
        NotificationParameters parameters = new NotificationParameters();
        notification.setParams(parameters);
        notification.setMethod(Method.ACCOUNT);
        parameters.setUUID(UUID.randomUUID().toString());
        AccountNotificationData data = new AccountNotificationData();
        parameters.setData(data);

        data.setOrderId(TestData.TRUSTLY_ORDER_ID);
        data.setAccountId(TestData.TRUSTLY_ACCOUNT_ID);
        data.setMessageId(TestData.HEDVIG_ORDER_ID.toString());
        data.setNotificationId(trustlyNotificationId);
        data.setVerified(true);

        final HashMap<String, Object> attributes = new HashMap<>();

        attributes.put("descriptor", TestData.TRUSTLY_ACCOUNT_DESCRIPTOR);
        attributes.put("bank", TestData.TRUSTLY_ACCOUNT_BANK);
        attributes.put("clearinghouse", TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE);
        attributes.put("lastdigits", TestData.TRUSTLY_ACCOUNT_LAST_DIGITS);
        if(directDebitMandate != null) {
            attributes.put("directdebitmandate", directDebitMandate ? "1" : "0");
        }
        data.setAttributes(attributes);

        return notification;
    }

    private SelectAccountResponseReceivedEvent selectAccountResponseReceivedEvent() {
        return new SelectAccountResponseReceivedEvent(TestData.HEDVIG_ORDER_ID, TestData.TRUSTLY_IFRAME_URL);
    }

    private OrderAssignedTrustlyIdEvent orderAssignedTrustlyIdEvent() {
        return new OrderAssignedTrustlyIdEvent(TestData.HEDVIG_ORDER_ID, TestData.TRUSTLY_ORDER_ID);
    }

    private SelectAccountResponseReceivedCommand selectAccountCommand() {
        return new SelectAccountResponseReceivedCommand(TestData.HEDVIG_ORDER_ID, TestData.TRUSTLY_IFRAME_URL, TestData.TRUSTLY_ORDER_ID);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(TestData.HEDVIG_ORDER_ID, TestData.MEMBER_ID);
    }
}