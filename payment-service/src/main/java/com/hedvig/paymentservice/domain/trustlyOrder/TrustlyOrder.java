
package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentService.trustly.data.notification.NotificationData;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.AccountNotificationData;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.NotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class TrustlyOrder {

    Logger log = LoggerFactory.getLogger(TrustlyOrder.class);

    @AggregateIdentifier
    private UUID id;
    private String trustlyOrderId;
    private OrderType orderType;
    private OrderState orderState;
    private String memberId;

    public TrustlyOrder() {}

    @CommandHandler
    public TrustlyOrder(CreateOrderCommand cmd) {

        apply(new OrderCreatedEvent(cmd.getHedvigOrderId(), cmd.getMemberId()));
    }

    @CommandHandler
    public void cmd(SelectAccountResponseReceivedCommand cmd) {
        apply(new OrderAssignedTrustlyIdEvent(cmd.getHedvigOrderId(), cmd.getTrustlyOrderId()));
        apply(new SelectAccountResponseReceivedEvent(cmd.getHedvigOrderId(), cmd.getIframeUrl()));
    }

    @CommandHandler
    public void cmd(NotificationReceivedCommand cmd) {
        final NotificationData data = cmd.getNotification().getParams().getData();
        apply(new NotificationReceivedEvent(data.getNotificationId(), data.getOrderId()));
        switch (cmd.getNotification().getMethod()) {
            case ACCOUNT:
                try{
                    handleAccountNotificaiton(data);
                }catch (Exception e) {
                    final String logMessage = String.format("Caugth exception handling trustly account notification with orderId: %s", data.getOrderId());
                    log.error(logMessage, e);
                }

                if(orderState == OrderState.CONFIRMED) {

                    apply(new OrderCompletedEvent(this.id));
                }
                break;
            case CANCEL:
                apply(new OrderCanceledEvent(this.id));
                break;
        }
    }

    private void handleAccountNotificaiton(NotificationData data) {
        AccountNotificationData accountData = (AccountNotificationData) data;
        final Map<String, Object> attributes = accountData.getAttributes();
        String directDebitMandate = (String) attributes.getOrDefault("directdebitmandate", "0");
        String lastDigits = (String) attributes.get("lastdigits");
        String clearingHouse = (String) attributes.get("clearinghouse");
        String bank = (String) attributes.get("bank");
        String descriptor = (String) attributes.get("descriptor");
        String personId = (String) attributes.get("personid");
        String name = (String) attributes.get("name");
        String address = (String) attributes.get("address");
        String zipCode = (String) attributes.get("zipcode");
        String city = (String) attributes.get("city");

        String accountId = accountData.getAccountId();
        apply(new AccountNotificationReceivedEvent(
                data.getNotificationId(),
                data.getOrderId(),
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
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent e) {
        this.id = e.getHedvigOrderId();
        this.memberId = e.getMemberId();
    }

    @EventSourcingHandler
    public void on(OrderAssignedTrustlyIdEvent e) {
        this.trustlyOrderId = e.getTrustlyOrderId();
        this.orderState = OrderState.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(SelectAccountResponseReceivedEvent e) {
        this.orderType = OrderType.SELECT_ACCOUNT;
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent e) {
        this.orderState = OrderState.COMPLETE;
    }

    @EventSourcingHandler
    public void on(OrderCanceledEvent e) {
        this.orderState = OrderState.CANCELED;
    }

}
