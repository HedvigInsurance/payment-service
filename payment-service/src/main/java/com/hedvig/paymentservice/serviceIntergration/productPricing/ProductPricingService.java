package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;

import java.util.Optional;

public interface ProductPricingService {
  Optional<InsuranceStatus> getInsuranceStatus(String memberId);
}
