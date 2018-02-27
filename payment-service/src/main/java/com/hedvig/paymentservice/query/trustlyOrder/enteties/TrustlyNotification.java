package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class TrustlyNotification {

    @Id
    String notificationId;

    @ManyToOne
    @JoinColumn(name="order_id", nullable = false)
    TrustlyOrder order;

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
