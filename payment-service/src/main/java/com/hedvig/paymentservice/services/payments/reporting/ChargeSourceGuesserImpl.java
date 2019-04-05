package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
  public Map<UUID, Optional<PolicyGuessResponseDto>> guessChargesMetadata(final Collection<Transaction> transactions, final YearMonth period) {
    log.info("Guessing charge metadata for {} transactions", transactions.size());

    return productPricingService.guessPolicyTypes(transactions, period).entrySet().stream()
      .peek(entry -> {
        if (!entry.getValue().isPresent()) {
          log.error("Unsure about guess for transaction {}", entry.getKey());
        } else {
          log.info(
            "Guessed transaction {} to be {} of {}",
            entry.getKey(),
            entry.getValue().get().getProductType(),
            entry.getValue().get().getInceptionInStockholm()
          );
        }
      })
      .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
