package com.hedvig.paymentservice.web.internal;

import javax.transaction.Transactional;
import lombok.val;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.data.response.Result;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
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

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PayoutIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private EventStore eventStore;

    @MockBean
    private SignedAPI signedApi;

    @MockBean
    private UUIDGenerator uuidGenerator;

    @Test
    public void givenMemberWithoutTrustlyAccount_WhenCreatingPayout_ThenShouldReturnForbidden() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(MEMBER_ID));

        val payoutRequest = new PayoutRequest(
            TRANSACTION_AMOUNT,
            TOLVANSSON_STREET,
            COUNTRY_CODE,
            TOLVANSSON_DATE_OF_BIRTH,
            TOLVAN_FIRST_NAME,
            TOLVANSSON_LAST_NAME
        );

        mockMvc
            .perform(
                post(String.format("/_/members/%s/payout", MEMBER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payoutRequest)))
            .andExpect(status().is(403));

        val memberEvents = eventStore
            .readEvents(MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());

        assertTrue(memberEvents.get(1).getPayload() instanceof PayoutCreationFailedEvent);
    }

    @Test
    public void givenMemberWithTrustlyAccount_WhenCreatingPayoutAndTrustlyReturnsSuccess_ThenShouldReturnAccepted() throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(MEMBER_ID));
        commandGateway.sendAndWait(new UpdateTrustlyAccountCommand(
                MEMBER_ID,
                HEDVIG_ORDER_ID,
                TRUSTLY_ACCOUNT_ID,
                TOLVANSSON_STREET,
                TRUSTLY_ACCOUNT_BANK,
                TOLVANSSON_CITY,
                TRUSTLY_ACCOUNT_CLEARING_HOUSE,
                TRUSTLY_ACCOUNT_DESCRIPTOR,
                TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE,
                TRUSTLY_ACCOUNT_LAST_DIGITS,
                TOLVAN_FIRST_NAME + " " + TOLVANSSON_LAST_NAME,
                TOLVANSSON_SSN,
                TOLVANSSON_ZIP
        ));

        val payoutRequest = new PayoutRequest(
            TRANSACTION_AMOUNT,
            TOLVANSSON_STREET,
            COUNTRY_CODE,
            TOLVANSSON_DATE_OF_BIRTH,
            TOLVAN_FIRST_NAME,
            TOLVANSSON_LAST_NAME
        );

        mockTrustlyApiResponse(true);
        given(uuidGenerator.generateRandom())
            .willReturn(HEDVIG_ORDER_ID);

        mockMvc
            .perform(
                post(String.format("/_/members/%s/payout", MEMBER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payoutRequest)))
            .andExpect(status().is(202));

        val memberEvents = eventStore
            .readEvents(MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());

        memberEvents.forEach(e -> System.out.println(e.getPayloadType().toString()));
        assertTrue(memberEvents.get(2).getPayload() instanceof PayoutCreatedEvent);
        assertTrue(memberEvents.get(3).getPayload() instanceof PayoutCompletedEvent);
    }

    private void mockTrustlyApiResponse(boolean shouldSucceed) {
        val trustlyResultData = new HashMap<String, Object>();
        trustlyResultData.put("orderid", TRUSTLY_ORDER_ID);

        val trustlyResult = new Result();
        trustlyResult.setData(trustlyResultData);
        val trustlyApiResponse = new Response();

        if (shouldSucceed) {
            trustlyApiResponse.setResult(trustlyResult);
        } else {
            val error = new Error();
            trustlyApiResponse.setError(error);
        }

        given(signedApi.sendRequest(any()))
            .willReturn(trustlyApiResponse);
    }

}
