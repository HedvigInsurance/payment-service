package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Entity
@Getter
@Setter
public class TrustlyOrder {
    @Id
    UUID id;

    String memberId;

    String trustlyOrderId;

    @Enumerated(EnumType.STRING)
    OrderState state;

    @Enumerated(EnumType.STRING)
    OrderType type;

    @Column(length = 1024)
    String iframeUrl;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<TrustlyNotification> notifications = new TreeSet<>();

    public void addNotification(TrustlyNotification notification) {
        notifications.add(notification);
        notification.setOrder(this);
    }
}
