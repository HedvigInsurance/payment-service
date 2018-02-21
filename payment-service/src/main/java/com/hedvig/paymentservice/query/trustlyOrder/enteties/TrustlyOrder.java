package com.hedvig.paymentservice.query.trustlyOrder.enteties;

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
public class TrustlyOrder {

    @Id
    UUID id;

    String trustlyOrderId;

    @Enumerated(EnumType.STRING)
    OrderState state;

    @Enumerated(EnumType.STRING)
    OrderType type;

    @Column(length = 1024)
    String iframeUrl;

}
