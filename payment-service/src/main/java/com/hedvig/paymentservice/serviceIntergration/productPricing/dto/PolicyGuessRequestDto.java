package com.hedvig.paymentservice.serviceIntergration.productPricing.dto;

import com.hedvig.paymentservice.query.member.entities.Transaction;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class PolicyGuessRequestDto {
  private final UUID id;
  private final String memberId;
  private final LocalDate date;

  public PolicyGuessRequestDto(final UUID id, final String memberId, final LocalDate date) {
    this.id = id;
    this.memberId = memberId;
    this.date = date;
  }

  public UUID getId() {
    return id;
  }

  public String getMemberId() {
    return memberId;
  }

  public LocalDate getDate() {
    return date;
  }

  public static PolicyGuessRequestDto from(final Transaction transaction) {
    return new PolicyGuessRequestDto(
      transaction.getId(),
      transaction.getMember().getId(),
      transaction.getTimestamp().atZone(ZoneId.of("Europe/Stockholm")).toLocalDate()
    );
  }
}
