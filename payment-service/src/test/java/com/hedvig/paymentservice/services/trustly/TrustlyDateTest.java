package com.hedvig.paymentservice.services.trustly;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
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
    val dateTimeFormatter = getDateTimeFormatter();
    val ta = dateTimeFormatter.parse(trustlyDate);
    val inst = Instant.from(ta);
    // This test should simply not raise an exception :)
    System.out.println(inst.toString());
  }

  @Test
  public void shouldAlsoParseDateCorrectly() {
    val dateTimeFormatter = getDateTimeFormatter();

    val ta = dateTimeFormatter.parse(trustlyDate2);
    val inst = Instant.from(ta);
    // This test should simply not raise an exception :)
    System.out.println(inst.toString());
  }

  public DateTimeFormatter getDateTimeFormatter() {
    return TrustlyService.getDateTimeFormatter();
  }
}
