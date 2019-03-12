package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChargeSourceGuesserImplTest {
  @Test
  public void testGuessesChargeInsuranceType() {
    final ProductPricingService productPricingServiceStub = mock(ProductPricingService.class);
    final ChargeSourceGuesser chargeSourceGuesser = new ChargeSourceGuesserImpl(productPricingServiceStub);

    final Collection<Transaction> transactions = new ArrayList<>();
    final Map<UUID, Optional<PolicyType>> policyTypes = new HashMap<>();
    final UUID policyId = UUID.randomUUID();
    policyTypes.put(policyId, Optional.of(PolicyType.BRF));
    when(productPricingServiceStub.guessPolicyTypes(any())).thenReturn(policyTypes);

    final Map<UUID, ChargeSource> result = chargeSourceGuesser.guessChargesInsuranceTypes(transactions);
    assertEquals(result.size(), 1);
    assertEquals(result.get(policyId), ChargeSource.HOUSEHOLD_INSURANCE);
  }
}
