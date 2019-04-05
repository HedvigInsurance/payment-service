package com.hedvig.paymentservice.services.payments.reporting;

import com.hedvig.paymentservice.query.member.entities.Transaction;

import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ChargeSourceGuesser {
  Map<UUID, ChargeSource> guessChargesMetadata(Collection<Transaction> transactions, YearMonth period);
}
