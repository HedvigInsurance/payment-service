package com.hedvig.paymentservice.services.account;

import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;

import java.time.LocalDate;

public interface AccountService {
  BankAccount getBankAccount(String memberId);

  LocalDate getNextChargeDate(String memberId);

  DirectDebitStatus getDirectDebitStatus(String memberId);
}
