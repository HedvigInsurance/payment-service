package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyType;

import java.util.Optional;

public enum ChargeSource {
  STUDENT_INSURANCE,
  HOUSEHOLD_INSURANCE,
  UNSURE;

  public static ChargeSource from(final Optional<PolicyType> policyType) {
    if (!policyType.isPresent()) {
      return ChargeSource.UNSURE;
    }

    if (policyType.get().equals(PolicyType.BRF) || policyType.get().equals(PolicyType.RENT)) {
      return ChargeSource.HOUSEHOLD_INSURANCE;
    }

    return ChargeSource.STUDENT_INSURANCE;
  }
}
