package com.hedvig.paymentservice.services.bankAccounts;

import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.ChargeablePaymentMethodStatus;
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;

import java.time.LocalDate;

public interface BankAccountService {
  BankAccount getBankAccount(String memberId);

  LocalDate getNextChargeDate(String memberId);

  DirectDebitStatus getDirectDebitStatus(String memberId);

  ChargeablePaymentMethodStatus getChargeablePaymentMethodStatus(String memberId);
}
