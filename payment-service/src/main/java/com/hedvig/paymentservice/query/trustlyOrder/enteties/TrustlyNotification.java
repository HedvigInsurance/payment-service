package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class TrustlyNotification {

  @Id String notificationId;

  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
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
