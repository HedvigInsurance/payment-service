package com.hedvig.paymentservice.trustly.testHelpers;

import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;

public class TestData {
    public static final String TRIGGER_ID = "7fece3ca-17d9-11e8-8c15-f36f3d1de091";

    public static DirectDebitRequest createDirectDebitRequest() {
        return new DirectDebitRequest(
            "Tolvan",
                "Tolvansson",
                "19121212-1212",
                "tolvan@somewhere.com",
                "1337",
                TRIGGER_ID);
    }
}
