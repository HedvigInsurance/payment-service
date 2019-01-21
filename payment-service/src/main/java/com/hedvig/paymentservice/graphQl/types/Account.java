package com.hedvig.paymentservice.graphQl.types;

import com.hedvig.paymentservice.query.member.entities.Member;
import lombok.Value;

@Value
public class Account {

  String bankName;
  String lastDigits;

  public static Account fromMember(Member m) {
    return new Account(m.getBank(), m.getLastDigits());
  }
}
