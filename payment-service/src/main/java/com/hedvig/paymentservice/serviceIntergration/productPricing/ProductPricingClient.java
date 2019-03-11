package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.configuration.FeignConfiguration;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
  name = "productPricingClient",
  url = "${hedvig.product-pricing.url:product-pricing}",
  configuration = FeignConfiguration.class)
public interface ProductPricingClient {

  @RequestMapping(value = "/i/insurance/{memberId}/status", method = RequestMethod.GET)
  ResponseEntity<InsuranceStatus> getInsuranceStatus(@PathVariable("memberId") String memberId);
}

