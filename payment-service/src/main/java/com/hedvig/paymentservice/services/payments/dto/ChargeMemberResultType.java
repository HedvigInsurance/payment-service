package com.hedvig.paymentservice.services.payments.dto;

public enum ChargeMemberResultType {
  NO_TRUSTLY_ACCOUNT,
  NO_DIRECT_DEBIT,
  SUCCESS,
  NO_PAYIN_METHOD_FOUND,
  ADYEN_NOT_AUTHORISED,
  CURRENCY_MISMATCH
}
