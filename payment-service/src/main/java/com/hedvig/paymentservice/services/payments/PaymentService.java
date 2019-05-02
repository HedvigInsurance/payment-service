package com.hedvig.paymentservice.services.payments;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.services.Helpers;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequestDTO;
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

  public void createMember(String memberId) {
    commandGateway.sendAndWait(new CreateMemberCommand(memberId));
  }

  public boolean chargeMember(ChargeMemberRequest request) {
    val transactionId = uuidGenerator.generateRandom();
    return commandGateway.sendAndWait(
        new CreateChargeCommand(
            request.getMemberId(),
            transactionId,
            request.getAmount(),
            Instant.now(),
            Helpers.createTrustlyInboxfromMemberId(request.getMemberId())));
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
            request.getHandler()
        )
    );

    return result ? Optional.of(transactionId) : Optional.empty();
  }

  public void sendCommand(UpdateTrustlyAccountCommand cmd) {
    commandGateway.sendAndWait(cmd);
  }
}
