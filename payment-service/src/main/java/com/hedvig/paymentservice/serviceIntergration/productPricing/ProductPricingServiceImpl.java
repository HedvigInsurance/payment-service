package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ProductPricingServiceImpl implements ProductPricingService {
  private ProductPricingClient productPricingClient;

  @Autowired
  public ProductPricingServiceImpl(ProductPricingClient productPricingClient) {
    this.productPricingClient = productPricingClient;
  }

  @Override
  public Optional<InsuranceStatus> getInsuranceStatus(String memberId) {
    try {
      ResponseEntity<InsuranceStatus> response = productPricingClient.getInsuranceStatus(memberId);
      return response.getStatusCode().is2xxSuccessful() ? Optional.ofNullable(response.getBody()) : Optional.empty();
    } catch (FeignException ex) {
      switch (ex.status()){
        case 500:
          log.error("Product-pricing returned 500 response");
        case 404:
          return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
