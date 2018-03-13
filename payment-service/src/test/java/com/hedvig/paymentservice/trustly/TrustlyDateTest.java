package com.hedvig.paymentservice.trustly;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyDateTest {
    private static final String trustlyDate = "2018-03-12 10:00:00.944584+00";
    @Test
    public void shouldParseDateCorrectly() {
        val dateString = "uuuu-MM-dd HH:mm:ss.SSSSSSx";
        val dateTimeFormatter = DateTimeFormatter.ofPattern(dateString);
        val ta = dateTimeFormatter.parse(trustlyDate);
        val inst = Instant.from(ta);
        // This test should simply not raise an exception :)
        System.out.println(inst.toString());
    }
}
