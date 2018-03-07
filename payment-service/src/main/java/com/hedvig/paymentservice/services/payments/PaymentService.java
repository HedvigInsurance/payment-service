package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import java.time.Instant;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final CommandGateway commandGateway;
    private final UUIDGenerator uuidGenerator;

    public PaymentService(CommandGateway commandGateway, UUIDGenerator uuidGenerator) {
        this.commandGateway = commandGateway;
        this.uuidGenerator = uuidGenerator;
    }

    public void chargeMember(ChargeMemberRequest request) {
        val transactionId = uuidGenerator.generateRandom();
        commandGateway.sendAndWait(new CreateChargeCommand(
            request.getMemberId(),
            transactionId,
            request.getAmount(),
            request.getCurrency(),
            Instant.now(),
            request.getEmail()
        ));
    }
}
