package com.hedvig.paymentservice.trustly;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyDateTest {
    private static final String trustlyDate = "2018-03-12 10:00:00.944584+00";
    private static final String instantDate = "2011-12-03T10:15:30Z";
    @Test
    public void shouldParseDateCorrectly() {
        val dateString = "uuuu-MM-dd hh:mm:ss.SSSSSSx";
        val dateTimeFormatter = DateTimeFormatter.ofPattern(dateString);
        val dateTime = OffsetDateTime.parse(trustlyDate, dateTimeFormatter);
        // val inst = dateTime.toInstant();
        System.out.println(dateTime.toString());
        // val time = Instant.parse();
        // val dateFormat = LocalDate.parse(, dateTimeFormatter);
    }

    @Test
    public void canParseDateCorrectly() {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-mm-dd");
    }
}
