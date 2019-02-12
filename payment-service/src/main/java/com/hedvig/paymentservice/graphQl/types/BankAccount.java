package com.hedvig.paymentservice.graphQl.types;

import com.hedvig.paymentservice.query.member.entities.Member;
import lombok.Value;

@Value
public class BankAccount {

  String bankName;
  String descriptor;
  DirectDebitStatus directDebitStatus;

  public static BankAccount fromMember(Member m) {
    return new BankAccount(m.getBank(), m.getDescriptor(), fromMemberDirectStatus(m.getDirectDebitStatus()));
  }

  private static DirectDebitStatus fromMemberDirectStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus s) {
    switch (s) {
      case CONNECTED:
        return DirectDebitStatus.ACTIVE;
      case PENDING:
        return DirectDebitStatus.PENDING;
      case DISCONNECTED:
        return DirectDebitStatus.NEEDS_SETUP;
      default:
        return null;
    }
  }
}
