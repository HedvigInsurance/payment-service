package com.hedvig.paymentservice.serviceIntergration.productPricing.dto;

import java.time.LocalDate;

public class PolicyGuessResponseDto {
  private PolicyType productType;
  private LocalDate inceptionInStockholm;

  public PolicyGuessResponseDto() {
  }

  public PolicyGuessResponseDto(final PolicyType productType, final LocalDate inceptionInStockholm) {
    this.productType = productType;
    this.inceptionInStockholm = inceptionInStockholm;
  }

  public PolicyType getProductType() {
    return productType;
  }

  public LocalDate getInceptionInStockholm() {
    return inceptionInStockholm;
  }
}
