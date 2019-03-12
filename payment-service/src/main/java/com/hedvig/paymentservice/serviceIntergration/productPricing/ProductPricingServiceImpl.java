package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyType;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class ProductPricingServiceImpl implements ProductPricingService {
  private final ProductPricingClient client;

  public ProductPricingServiceImpl(final ProductPricingClient client) {
    this.client = client;
  }

  @Override
  public Optional<InsuranceStatus> getInsuranceStatus(String memberId) {
    try {
      ResponseEntity<InsuranceStatus> response = client.getInsuranceStatus(memberId);
      return response.getStatusCode().is2xxSuccessful() ? Optional.ofNullable(response.getBody()) : Optional.empty();
    } catch (FeignException ex) {
      switch (ex.status()) {
        case 500:
          log.error("Product-pricing returned 500 response");
        case 404:
          return Optional.empty();
      }
    }
    return Optional.empty();
  }

  @Override
  public Map<UUID, Optional<PolicyType>> guessPolicyTypes(final Collection<Transaction> transactions) {
    final Collection<PolicyGuessRequestDto> policyGuessDtos = transactions.stream()
      .map(PolicyGuessRequestDto::from)
      .collect(toList());
    return client.guessPolicyTypes(policyGuessDtos).getBody();
  }
}
