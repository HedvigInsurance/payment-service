package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class TrustlyOrder {
  @Id
  UUID id;

  String memberId;

  String trustlyOrderId;

  @Enumerated(EnumType.STRING)
  public OrderState state;

  @Enumerated(EnumType.STRING)
  OrderType type;

  @Column(length = 1024)
  public String iframeUrl;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  Set<TrustlyNotification> notifications = new HashSet<>();

    public TrustlyOrder() {
    }

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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TrustlyOrder)) return false;
        final TrustlyOrder other = (TrustlyOrder) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$memberId = this.getMemberId();
        final Object other$memberId = other.getMemberId();
        if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
        final Object this$trustlyOrderId = this.getTrustlyOrderId();
        final Object other$trustlyOrderId = other.getTrustlyOrderId();
        if (this$trustlyOrderId == null ? other$trustlyOrderId != null : !this$trustlyOrderId.equals(other$trustlyOrderId))
            return false;
        final Object this$state = this.getState();
        final Object other$state = other.getState();
        if (this$state == null ? other$state != null : !this$state.equals(other$state)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$iframeUrl = this.getIframeUrl();
        final Object other$iframeUrl = other.getIframeUrl();
        if (this$iframeUrl == null ? other$iframeUrl != null : !this$iframeUrl.equals(other$iframeUrl)) return false;
        final Object this$notifications = this.getNotifications();
        final Object other$notifications = other.getNotifications();
        if (this$notifications == null ? other$notifications != null : !this$notifications.equals(other$notifications))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TrustlyOrder;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $memberId = this.getMemberId();
        result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
        final Object $trustlyOrderId = this.getTrustlyOrderId();
        result = result * PRIME + ($trustlyOrderId == null ? 43 : $trustlyOrderId.hashCode());
        final Object $state = this.getState();
        result = result * PRIME + ($state == null ? 43 : $state.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $iframeUrl = this.getIframeUrl();
        result = result * PRIME + ($iframeUrl == null ? 43 : $iframeUrl.hashCode());
        final Object $notifications = this.getNotifications();
        result = result * PRIME + ($notifications == null ? 43 : $notifications.hashCode());
        return result;
    }

    public String toString() {
        return "TrustlyOrder(id=" + this.getId() + ", memberId=" + this.getMemberId() + ", trustlyOrderId=" + this.getTrustlyOrderId() + ", state=" + this.getState() + ", type=" + this.getType() + ", iframeUrl=" + this.getIframeUrl() + ", notifications=" + this.getNotifications() + ")";
    }
}
