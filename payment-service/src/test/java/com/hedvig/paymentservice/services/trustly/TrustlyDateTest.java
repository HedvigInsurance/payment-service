package com.hedvig.paymentservice.services.trustly;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyDateTest {
  private static final String trustlyDate = "2018-03-12 10:00:00.944584+00";
  private static final String trustlyDate2 = "2018-06-08 15:18:30.0477+02";

  @Test
  public void shouldParseDateCorrectly() {
      final DateTimeFormatter dateTimeFormatter = getDateTimeFormatter();
      final TemporalAccessor ta = dateTimeFormatter.parse(trustlyDate);
      final Instant inst = Instant.from(ta);
    // This test should simply not raise an exception :)
    System.out.println(inst.toString());
  }

  @Test
  public void shouldAlsoParseDateCorrectly() {
      final DateTimeFormatter dateTimeFormatter = getDateTimeFormatter();

      final TemporalAccessor ta = dateTimeFormatter.parse(trustlyDate2);
      final Instant inst = Instant.from(ta);
    // This test should simply not raise an exception :)
    System.out.println(inst.toString());
  }

  public DateTimeFormatter getDateTimeFormatter() {
    return TrustlyService.Companion.getDateTimeFormatter();
  }
}
