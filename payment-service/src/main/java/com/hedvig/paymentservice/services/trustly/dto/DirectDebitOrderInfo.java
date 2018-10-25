package com.hedvig.paymentservice.services.trustly.dto;

import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO;
import lombok.Value;

@Value
public class DirectDebitOrderInfo {

  String memberId;
  String personalNumber;
  String firstName;
  String lastName;
  String triggerId;
  boolean redirectingToBotService;

  public DirectDebitOrderInfo(DirectDebitRequest request, boolean isRedirectingToBotService) {
    this.memberId = request.getMemberId();
    this.personalNumber = request.getSsn();
    this.firstName = request.getFirstName();
    this.lastName = request.getLastName();
    this.triggerId = request.getTriggerId();
    this.redirectingToBotService = isRedirectingToBotService;
  }

  public DirectDebitOrderInfo(
      String memberId,
      RegisterDirectDebitRequestDTO request,
      boolean isRedirectingToBotService) {
    this.memberId = memberId;
    this.personalNumber = request.getPersonalNumber();
    this.firstName = request.getFirstName();
    this.lastName = request.getLastName();
    this.triggerId = null;
    this.redirectingToBotService = isRedirectingToBotService;
  }
}
