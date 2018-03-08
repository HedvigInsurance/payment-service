package com.hedvig.paymentservice.web.internal;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import lombok.val;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;

import javax.money.MonetaryAmount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.CreateTrustlyAccountCommand;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import java.util.HashMap;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.data.response.Result;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommandGateway commandGateway;

    @MockBean
    private SignedAPI signedApi;

    private static final String MEMBER_ID = "1";
    private static final String TRUSTLY_ACCOUNT_ID = "1";
    private static final String EMAIL = "test@hedvig.com";
    private static final MonetaryAmount MONETARY_AMOUNT = Money.of(100, "SEK");
    private static final String ORDER_ID = "123";
    private static final String PAYMENT_URL = "testurl";

    @Test
    // TODO rename test
    public void givenMemberWithoutDirectDebitMandate_WhenCreatingCharge_ThenShouldReturnForbidden() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(MEMBER_ID));

        val chargeRequest = new ChargeRequest(MONETARY_AMOUNT, EMAIL);

        mockMvc
            .perform(
                post(
                    String.format(
                        "/_/members/%s/charge",
                        MEMBER_ID
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andExpect(status().is(403));
    }

    @Test
    public void givenMemberWithDirectDebitMandate_WhenCreatingCharge_ThenShouldReturnAccepted() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(MEMBER_ID));
        commandGateway.sendAndWait(new CreateTrustlyAccountCommand(MEMBER_ID, TRUSTLY_ACCOUNT_ID));

        mockTrustlyApiResponse();
        val chargeRequest = new ChargeRequest(MONETARY_AMOUNT, EMAIL);

        mockMvc
            .perform(
                post(
                    String.format(
                        "/_/members/%s/charge",
                        MEMBER_ID
                    )
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
            )
            .andExpect(status().is(202));
    }

    private void mockTrustlyApiResponse() {
        val trustlyResultData = new HashMap<String, Object>();
        trustlyResultData.put("orderid", ORDER_ID);
        trustlyResultData.put("url", PAYMENT_URL);
        val trustlyResult = new Result();
        trustlyResult.setData(trustlyResultData);
        val trustlyApiResponse = new Response();
        trustlyApiResponse.setResult(trustlyResult);

        given(
            signedApi.sendRequest(
                any()
            )
        )
        .willReturn(trustlyApiResponse);
    }
}
