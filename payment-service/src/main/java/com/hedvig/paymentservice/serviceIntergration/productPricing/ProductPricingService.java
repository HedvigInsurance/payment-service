package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ProductPricingService {
  Optional<InsuranceStatus> getInsuranceStatus(String memberId);

  Map<UUID, Optional<PolicyGuessResponseDto>> guessPolicyTypes(Collection<Transaction> transactions, YearMonth period);
}
