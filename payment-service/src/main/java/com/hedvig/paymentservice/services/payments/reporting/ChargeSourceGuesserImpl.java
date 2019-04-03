package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
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
  public Map<UUID, ChargeSource> guessChargesMetadata(final Collection<Transaction> transactions, final YearMonth period) {
    log.info("Guessing charge metadata for {} transactions", transactions.size());
    return productPricingService.guessPolicyTypes(transactions, period).entrySet().stream()
      .map(entry -> Pair.of(entry.getKey(), ChargeSource.from(entry.getValue().map(PolicyGuessResponseDto::getProductType))))
      .peek(entry -> {
        if (entry.getSecond().equals(ChargeSource.UNSURE)) {
          log.error("Unsure about insurance type for transaction {}", entry.getFirst());
        } else {
          log.info("Guessed transaction {} to be {}", entry.getFirst(), entry.getSecond());
        }
      })
      .collect(toMap(Pair::getFirst, Pair::getSecond));
  }
}
