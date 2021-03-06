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
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.ContractMarketInfo;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.DomainEventMessage;
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
import org.springframework.transaction.annotation.Transactional;

import javax.money.Monetary;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.hedvig.paymentservice.domain.DomainTestUtilities.hasEvent;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.CREATED_BY;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.HEDVIG_ORDER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_CITY;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_LAST_NAME;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_SSN;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_STREET;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_ZIP;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVAN_FIRST_NAME;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRANSACTION_AMOUNT;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_BANK;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_CLEARING_HOUSE;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_DESCRIPTOR;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_DIRECTDEBIT_TRUE;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_LAST_DIGITS;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ORDER_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ChargeIntegrationTest {
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

    @MockBean
    private ProductPricingService productPricingService;

    private static final String EMAIL = "test@hedvig.com";
    private static final String PAYMENT_URL = "testurl";

    @Test
    public void givenMemberWithoutDirectDebitMandate_WhenCreatingCharge_ThenShouldReturnForbidden()
        throws Exception {

        given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);
        given(productPricingService.getContractMarketInfo(any())).willReturn(new ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK")));
        commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));

        final ChargeRequest chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT, CREATED_BY);

        mockMvc
            .perform(
                post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)))
            .andExpect(status().isForbidden());

        final List<? extends DomainEventMessage<?>> memberEvents =
            eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

        assertThat(memberEvents, hasEvent(ChargeCreationFailedEvent.class));
    }

    @Test
    public void
    givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsSuccess_ThenShouldReturnAccepted()
        throws Exception {
        given(productPricingService.getContractMarketInfo(any())).willReturn(new ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK")));

        commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));
        commandGateway.sendAndWait(
            new UpdateTrustlyAccountCommand(
                TOLVANSSON_MEMBER_ID,
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
                TOLVANSSON_ZIP));

        mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_SUCCEED);
        given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);

        final ChargeRequest chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT, CREATED_BY);

        mockMvc
            .perform(
                post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)))
            .andExpect(status().isAccepted());

        final List<? extends DomainEventMessage<?>> memberEvents =
            eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());
        assertThat(memberEvents, hasEvent(ChargeCreatedEvent.class));

        final List<? extends DomainEventMessage<?>> trustlyOrderEvents =
            eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
        assertThat(trustlyOrderEvents, hasEvent(PaymentResponseReceivedEvent.class));
    }

    @Test
    public void
    givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsError_ThenShouldReturnAccepted()
        throws Exception {
        given(productPricingService.getContractMarketInfo(any())).willReturn(new ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK")));

        commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));
        commandGateway.sendAndWait(
            new UpdateTrustlyAccountCommand(
                TOLVANSSON_MEMBER_ID,
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
                TOLVANSSON_ZIP));

        mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_FAIL);
        given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);

        final ChargeRequest chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT, CREATED_BY);

        mockMvc
            .perform(
                post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)))
            .andExpect(status().isAccepted());

        final List<? extends DomainEventMessage<?>> memberEvents =
            eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());
        assertThat(memberEvents, hasEvent(ChargeCreatedEvent.class));

        final List<? extends DomainEventMessage<?>> trustlyOrderEvents =
            eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
        assertThat(trustlyOrderEvents, hasEvent(PaymentErrorReceivedEvent.class));
    }

    @Test
    public void
    givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsSuccess_ThenShouldReturnTransactionId()
        throws Exception {
        commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));
        commandGateway.sendAndWait(
            new UpdateTrustlyAccountCommand(
                TOLVANSSON_MEMBER_ID,
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
                TOLVANSSON_ZIP));

        mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_SUCCEED);
        given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);
        given(productPricingService.getContractMarketInfo(any())).willReturn(new ContractMarketInfo(Market.SWEDEN, Monetary.getCurrency("SEK")));

        final ChargeRequest chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT, CREATED_BY);

        mockMvc
            .perform(
                post(String.format("/v2/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)))
            .andExpect(status().isAccepted()).andExpect(content().string('"' + HEDVIG_ORDER_ID.toString() + '"'));

        final List<? extends DomainEventMessage<?>> memberEvents =
            eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());
        assertThat(memberEvents, hasEvent(ChargeCreatedEvent.class));

        final List<? extends DomainEventMessage<?>> trustlyOrderEvents =
            eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
        assertThat(trustlyOrderEvents, hasEvent(PaymentResponseReceivedEvent.class));
    }

    private void mockTrustlyApiResponse(TrustlyApiResponseResult result) {
        final HashMap<String, Object> trustlyResultData = new HashMap<>();
        trustlyResultData.put("orderid", TRUSTLY_ORDER_ID);
        trustlyResultData.put("url", PAYMENT_URL);
        final Result trustlyResult = new Result();
        trustlyResult.setData(trustlyResultData);
        final Response trustlyApiResponse = new Response();
        if (result == TrustlyApiResponseResult.SHOULD_SUCCEED) {
            trustlyApiResponse.setResult(trustlyResult);
        } else {
            final Error error = new Error();
            trustlyApiResponse.setError(error);
        }

        given(signedApi.sendRequest(any(), any())).willReturn(trustlyApiResponse);
    }

    private enum TrustlyApiResponseResult {
        SHOULD_SUCCEED,
        SHOULD_FAIL
    }
}
