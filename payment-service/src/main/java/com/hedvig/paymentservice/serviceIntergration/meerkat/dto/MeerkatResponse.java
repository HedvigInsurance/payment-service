package com.hedvig.paymentservice.serviceIntergration.meerkat.dto;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import lombok.Value;

@Value
public class MeerkatResponse {

  private String query;
  private SanctionStatus result;
}
