package com.hedvig.paymentservice.services.payments;

import com.adyen.service.Payment;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.services.Helpers;
import com.hedvig.paymentservice.services.payments.dto.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import lombok.val;
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
        val transactionId = uuidGenerator.generateRandom();

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
            logger.info("No aggregate found for member" + request.getMemberId() +  "assume direct debit is not connected");
            return new ChargeMemberResult(
                transactionId,
                ChargeMemberResultType.NO_DIRECT_DEBIT
            );
        }
    }

    @Deprecated
    public boolean payoutMember(PayoutMemberRequest request) {
        val transactionId = uuidGenerator.generateRandom();
        return commandGateway.sendAndWait(
            new CreatePayoutCommand(
                request.getMemberId(),
                request.getAddress(),
                request.getCountryCode(),
                request.getDateOfBirth(),
                request.getFirstName(),
                request.getLastName(),
                transactionId,
                request.getAmount(),
                Instant.now(),
                TransactionCategory.CLAIM,
                null,
                null,
                null,
                null
            )
        );
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
                member.getEmail()
            )
        );

        return result ? Optional.of(transactionId) : Optional.empty();
    }

    public void sendCommand(UpdateTrustlyAccountCommand cmd) {
        commandGateway.sendAndWait(cmd);
    }
}
