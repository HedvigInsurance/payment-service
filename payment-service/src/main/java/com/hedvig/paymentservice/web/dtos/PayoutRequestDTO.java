package com.hedvig.paymentservice.web.dtos;

import javax.money.MonetaryAmount;
import lombok.Value;

@Value
public class PayoutRequestDTO {

  MonetaryAmount amount;
  boolean sanctionBypassed;
}
