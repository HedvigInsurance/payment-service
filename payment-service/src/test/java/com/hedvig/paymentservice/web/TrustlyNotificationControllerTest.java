package com.hedvig.paymentservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentService.trustly.commons.Currency;
import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.NotificationParameters;
import com.hedvig.paymentService.trustly.data.notification.notificationdata.CreditData;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import javax.transaction.Transactional;
import lombok.val;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TrustlyNotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Ignore("This test is not done yet")
    @Test
    public void givenAConfirmedTrustlyChargeOrder_whenReceivingNotification_thenShouldReturnOk() throws Exception {

        val request = createTrustlyCreditNotificationRequest();
        System.out.println("Request is: " + objectMapper.writeValueAsString(request));

        mockMvc
            .perform(
                post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

    }
}
