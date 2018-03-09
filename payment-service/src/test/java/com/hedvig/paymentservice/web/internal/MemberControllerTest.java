package com.hedvig.paymentservice.web.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.data.response.Result;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreationFailedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PaymentErrorReceivedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.events.PaymentResponseReceivedEvent;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
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
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {
    @Autowired
    private
    MockMvc mockMvc;

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

    private static final String EMAIL = "test@hedvig.com";
    private static final MonetaryAmount MONETARY_AMOUNT = Money.of(100, "SEK");
    private static final String ORDER_ID = "123";
    private static final String PAYMENT_URL = "testurl";

    @Test
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

        val memberEvents = eventStore
            .readEvents(MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());

        assertTrue(memberEvents.get(1).getPayload() instanceof ChargeCreationFailedEvent);

    }

    @Test
    public void givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsSuccess_ThenShouldReturnAccepted() throws Exception {
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

        mockTrustlyApiResponse(true);
        given(uuidGenerator.generateRandom())
            .willReturn(HEDVIG_ORDER_ID);

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

        val memberEvents = eventStore
            .readEvents(MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());
        assertTrue(memberEvents.get(2).getPayload() instanceof ChargeCreatedEvent);

        val trustlyOrderEvents = eventStore
            .readEvents(HEDVIG_ORDER_ID.toString())
            .asStream()
            .collect(Collectors.toList());
        assertTrue(trustlyOrderEvents.get(3).getPayload() instanceof PaymentResponseReceivedEvent);
    }

    @Test
    public void givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsError_ThenShouldReturnAccepted() throws Exception {
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

        mockTrustlyApiResponse(false);
        given(uuidGenerator.generateRandom())
            .willReturn(HEDVIG_ORDER_ID);

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

        val memberEvents = eventStore
            .readEvents(MEMBER_ID)
            .asStream()
            .collect(Collectors.toList());
        assertTrue(memberEvents.get(2).getPayload() instanceof ChargeCreatedEvent);

        val trustlyOrderEvents = eventStore
            .readEvents(HEDVIG_ORDER_ID.toString())
            .asStream()
            .collect(Collectors.toList());
        assertTrue(trustlyOrderEvents.get(2).getPayload() instanceof PaymentErrorReceivedEvent);
    }

    private void mockTrustlyApiResponse(boolean shouldSucceed) {
        val trustlyResultData = new HashMap<String, Object>();
        trustlyResultData.put("orderid", ORDER_ID);
        trustlyResultData.put("url", PAYMENT_URL);
        val trustlyResult = new Result();
        trustlyResult.setData(trustlyResultData);
        val trustlyApiResponse = new Response();
        if (shouldSucceed) {
            trustlyApiResponse.setResult(trustlyResult);
        } else {
            val error = new Error();
            trustlyApiResponse.setError(error);
        }

        given(
            signedApi.sendRequest(
                any()
            )
        )
        .willReturn(trustlyApiResponse);
    }
}
