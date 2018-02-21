package com.hedvig.paymentservice.domain.trustlyOrder.commands;

import com.hedvig.paymentService.trustly.data.response.Result;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

@Value
public class SelectAccountResponseReceviedCommand {
    @TargetAggregateIdentifier
    private final UUID hedvigOrderId;

    private final String iframeUrl;

    private final String trustlyOrderId;

}
