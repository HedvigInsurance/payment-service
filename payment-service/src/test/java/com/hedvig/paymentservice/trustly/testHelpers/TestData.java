package com.hedvig.paymentservice.trustly.testHelpers;

import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;

public class TestData {
    public static DirectDebitRequest createDirectDebitRequest() {
        return new DirectDebitRequest(
            "Tolvan",
                "Tolvansson",
                "19121212-1212",
                "tolvan@somewhere.com",
                "1337");
    }
}
