package com.hedvig.paymentservice.common;

import java.util.UUID;

public class UUIDGeneratorImpl implements UUIDGenerator {
  @Override
  public UUID generateRandom() {
    return UUID.randomUUID();
  }
}
