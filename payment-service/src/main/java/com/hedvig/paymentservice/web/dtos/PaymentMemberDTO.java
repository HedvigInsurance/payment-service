package com.hedvig.paymentservice.web.dtos;

import com.hedvig.paymentservice.query.member.entities.Member;
import lombok.Value;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
public class PaymentMemberDTO {

  String id;
  Map<UUID, TransactionDTO> transactions;
  boolean directDebitMandateActive;
  String trustlyAccountNumber;

  public static PaymentMemberDTO fromMember(Member m) {
    return new PaymentMemberDTO(
      m.getId(),
      m.getTransactions().entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> TransactionDTO.fromTransaction(e.getValue()))),
      m.isDirectDebitMandateActive(),
      m.getTrustlyAccountNumber()
    );
  }
}

