package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.configuration.FeignConfiguration;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@FeignClient(
  name = "productPricingClient",
  url = "${hedvig.product-pricing.url:product-pricing}",
  configuration = FeignConfiguration.class)
public interface ProductPricingClient {

  @RequestMapping(value = "/i/insurance/{memberId}/status", method = RequestMethod.GET)
  ResponseEntity<InsuranceStatus> getInsuranceStatus(@PathVariable("memberId") String memberId);


  @PostMapping(path = "/report/policies/guess-types/{period}")
  ResponseEntity<Map<UUID, Optional<PolicyGuessResponseDto>>> guessPolicyTypes(
    @RequestBody Collection<PolicyGuessRequestDto> policiesToGuesses,
    @RequestParam("period") YearMonth period
  );
}
