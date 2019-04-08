package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessResponseDto;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ChargeSourceGuesser {
  Map<UUID, Optional<PolicyGuessResponseDto>> guessChargesMetadata(Collection<Transaction> transactions, YearMonth period);
}
