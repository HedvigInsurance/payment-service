package com.hedvig.paymentservice.graphQl.types;

import com.hedvig.paymentservice.query.member.entities.Member;
import lombok.Value;

@Value
public class BankAccount {

  String bankName;
  String descriptor;
  DirectDebitStatus directDebitStatus;

  public static BankAccount fromMember(Member m) {
    //TODO Resume work here
    return new BankAccount(m.getBank(), m.getDescriptor(), DirectDebitStatus.PENDING);
  }
}
