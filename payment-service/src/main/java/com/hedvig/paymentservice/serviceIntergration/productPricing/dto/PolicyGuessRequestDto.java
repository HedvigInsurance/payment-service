package com.hedvig.paymentservice.serviceIntergration.productPricing.dto;

import com.hedvig.paymentservice.query.member.entities.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PolicyGuessRequestDto {
  private final UUID id;
  private final String memberId;
  private final BigDecimal amount;
  private final Instant date;

  public PolicyGuessRequestDto(final UUID id, final String memberId, final BigDecimal amount, final Instant date) {
    this.id = id;
    this.memberId = memberId;
    this.amount = amount;
    this.date = date;
  }

  public UUID getId() {
    return id;
  }

  public String getMemberId() {
    return memberId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Instant getDate() {
    return date;
  }

  public static PolicyGuessRequestDto from(final Transaction transaction) {
    return new PolicyGuessRequestDto(
      transaction.getId(),
      transaction.getMember().getId(),
      transaction.getMoney().getNumber().numberValueExact(BigDecimal.class),
      transaction.getTimestamp());
  }
}
