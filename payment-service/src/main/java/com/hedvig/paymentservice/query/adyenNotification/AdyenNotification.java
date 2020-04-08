package com.hedvig.paymentservice.query.adyenNotification;

import com.adyen.model.notification.NotificationRequestItem;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
public class AdyenNotification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  Long value;
  String currency;
  String eventCode;
  String eventDate;
  String merchantAccountCode;
  String merchantReference;
  String originalReference;
  String pspReference;
  String reason;
  Boolean success;
  String paymentMethod;
  @ElementCollection
  List<String> operations = new ArrayList<>();
  @ElementCollection
  @MapKeyColumn(name = "key")
  @Column(name = "value")
  Map<String, String> additionalData = new HashMap<>();


  public AdyenNotification(long value, String currency, String eventCode, String eventDate, String merchantAccountCode, String merchantReference, String originalReference, String pspReference, String reason, boolean success, String paymentMethod, List<String> operations, Map<String, String> additionalData) {
    this.value = value;
    this.currency = currency;
    this.eventCode = eventCode;
    this.eventDate = eventDate;
    this.merchantAccountCode = merchantAccountCode;
    this.merchantReference = merchantReference;
    this.originalReference = originalReference;
    this.pspReference = pspReference;
    this.reason = reason;
    this.success = success;
    this.paymentMethod = paymentMethod;
    this.operations = operations;
    this.additionalData = additionalData;
  }

  public static AdyenNotification fromNotificationRequestItem(NotificationRequestItem item) {
    return new AdyenNotification(
      item.getAmount().getValue(),
      item.getAmount().getCurrency(),
      item.getEventCode(),
      item.getEventDate().toString(),
      item.getMerchantAccountCode(),
      item.getMerchantReference(),
      item.getOriginalReference(),
      item.getPspReference(),
      item.getReason(),
      item.isSuccess(),
      item.getPaymentMethod(),
      item.getOperations(),
      item.getAdditionalData()
    );
  }
}
