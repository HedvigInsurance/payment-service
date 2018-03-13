package com.hedvig.paymentservice.domain.trustlyOrder.events;

import lombok.Value;

import java.util.UUID;

@Value
public class AccountNotificationReceivedEvent {

    UUID hedvigOrderId;
    String memberId;

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
