package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.services.Helpers;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequestDTO;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.model.AggregateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final CommandGateway commandGateway;
    private final UUIDGenerator uuidGenerator;

    private static Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(CommandGateway commandGateway, UUIDGenerator uuidGenerator) {
        this.commandGateway = commandGateway;
        this.uuidGenerator = uuidGenerator;
    }

    public void createMember(String memberId) {
        commandGateway.sendAndWait(new CreateMemberCommand(memberId));
    }

    public ChargeMemberResult chargeMember(ChargeMemberRequest request) {
        UUID transactionId = uuidGenerator.generateRandom();

        try {
            return commandGateway.sendAndWait(
                new CreateChargeCommand(
                    request.getMemberId(),
                    transactionId,
                    request.getAmount(),
                    Instant.now(),
                    Helpers.createTrustlyInboxfromMemberId(request.getMemberId()),
                    request.getCreatedBy()
                ));
        } catch (AggregateNotFoundException exception) {
            logger.error("No aggregate found for member" + request.getMemberId() + "assume member has not connected their direct debit or card");
            return new ChargeMemberResult(
                transactionId,
                ChargeMemberResultType.NO_PAYIN_METHOD_FOUND
            );
        }
    }

    public Optional<UUID> payoutMember(String memberId, Member member, PayoutMemberRequestDTO request) {
        UUID transactionId = uuidGenerator.generateRandom();
        boolean result = commandGateway.sendAndWait(
            new CreatePayoutCommand(
                memberId,
                member.getStreet() + " " + member.getCity() + " " + member.getZipCode(),
                member.getCountry(),
                member.getBirthDate(),
                member.getFirstName(),
                member.getLastName(),
                transactionId,
                request.getAmount(),
                Instant.now(),
                request.getCategory(),
                request.getReferenceId(),
                request.getNote(),
                request.getHandler(),
                member.getEmail(),
                request.getCarrier()
            )
        );

        return result ? Optional.of(transactionId) : Optional.empty();
    }

    public void sendCommand(UpdateTrustlyAccountCommand cmd) {
        commandGateway.sendAndWait(cmd);
    }
}
