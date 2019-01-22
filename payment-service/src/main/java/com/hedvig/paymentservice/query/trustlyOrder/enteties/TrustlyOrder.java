package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
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

  public UUID getId() {
    return this.id;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public String getTrustlyOrderId() {
    return this.trustlyOrderId;
  }

  public OrderState getState() {
    return this.state;
  }

  public OrderType getType() {
    return this.type;
  }

  public String getIframeUrl() {
    return this.iframeUrl;
  }

  public Set<TrustlyNotification> getNotifications() {
    return this.notifications;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setTrustlyOrderId(String trustlyOrderId) {
    this.trustlyOrderId = trustlyOrderId;
  }

  public void setState(OrderState state) {
    this.state = state;
  }

  public void setType(OrderType type) {
    this.type = type;
  }

  public void setIframeUrl(String iframeUrl) {
    this.iframeUrl = iframeUrl;
  }

  public void setNotifications(Set<TrustlyNotification> notifications) {
    this.notifications = notifications;
  }
}
