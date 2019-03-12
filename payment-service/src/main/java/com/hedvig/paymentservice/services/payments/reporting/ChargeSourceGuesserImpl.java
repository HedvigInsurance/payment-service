package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class ChargeSourceGuesserImpl implements ChargeSourceGuesser {
  final ProductPricingService productPricingService;

  @Autowired
  public ChargeSourceGuesserImpl(final ProductPricingService productPricingService) {
    this.productPricingService = productPricingService;
  }

  @Override
  public Map<UUID, ChargeSource> guessChargesInsuranceTypes(final Collection<Transaction> transactions) {
    return productPricingService.guessPolicyTypes(transactions).entrySet().stream()
      .map(entry -> Pair.of(entry.getKey(), ChargeSource.from(entry.getValue())))
      .peek(entry -> {
        if (entry.getSecond().equals(ChargeSource.UNSURE)) {
          log.error("Unsure about insurance type for transaction {}", entry.getFirst());
        }
      })
      .collect(toMap(Pair::getFirst, Pair::getSecond));
  }
}
