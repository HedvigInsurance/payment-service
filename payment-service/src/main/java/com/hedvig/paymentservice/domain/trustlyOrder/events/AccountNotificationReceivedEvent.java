package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

@Value
public class AccountNotificationReceivedEvent {

    String notificationId;
    String trustlyOrderId;

    String accountId;
    String address;
    String bank;
    String city;
    String clearingHouse;
    String descriptor;
    Boolean directDebitMandate;
    String lastDigits;
    String name;
    String personId;
    String zipCode;


}
