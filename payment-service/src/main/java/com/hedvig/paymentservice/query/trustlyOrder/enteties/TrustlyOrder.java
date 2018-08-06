package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TrustlyOrder {
  @Id UUID id;

  String memberId;

  String trustlyOrderId;

  @Enumerated(EnumType.STRING)
  OrderState state;

  @Enumerated(EnumType.STRING)
  OrderType type;

  @Column(length = 1024)
  String iframeUrl;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  Set<TrustlyNotification> notifications = new HashSet<>();

  public void addNotification(TrustlyNotification notification) {
    notifications.add(notification);
    notification.setOrder(this);
  }
}
