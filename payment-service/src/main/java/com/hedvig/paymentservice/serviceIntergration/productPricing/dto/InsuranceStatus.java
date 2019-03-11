package com.hedvig.paymentservice.serviceIntergration.productPricing.dto;

public enum InsuranceStatus {
    PENDING, //Quote
    ACTIVE,  //Signed and within fromDate and toDate
    INACTIVE, //Signed waiting for old insurance termination;
    INACTIVE_WITH_START_DATE, //Siged with known start date
    TERMINATED; //Insurance is not active
}
