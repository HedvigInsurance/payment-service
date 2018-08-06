package com.hedvig.paymentservice.web.internal;

import static com.hedvig.paymentservice.domain.DomainTestUtilities.hasEvent;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.val;
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
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChargeIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CommandGateway commandGateway;

  @Autowired private EventStore eventStore;

  @MockBean private SignedAPI signedApi;

  @MockBean private UUIDGenerator uuidGenerator;

  private static final String EMAIL = "test@hedvig.com";
  private static final String PAYMENT_URL = "testurl";

  @Test
  public void givenMemberWithoutDirectDebitMandate_WhenCreatingCharge_ThenShouldReturnForbidden()
      throws Exception {
    commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));

    val chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT);

    mockMvc
        .perform(
            post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest)))
        .andExpect(status().isForbidden());

    val memberEvents =
        eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(ChargeCreationFailedEvent.class));
  }

  @Test
  public void
      givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsSuccess_ThenShouldReturnAccepted()
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

    val chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT);

    mockMvc
        .perform(
            post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest)))
        .andExpect(status().isAccepted());

    val memberEvents =
        eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());
    assertThat(memberEvents, hasEvent(ChargeCreatedEvent.class));

    val trustlyOrderEvents =
        eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
    assertThat(trustlyOrderEvents, hasEvent(PaymentResponseReceivedEvent.class));
  }

  @Test
  public void
      givenMemberWithDirectDebitMandate_WhenCreatingChargeAndTrustlyReturnsError_ThenShouldReturnAccepted()
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

    mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_FAIL);
    given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);

    val chargeRequest = new ChargeRequest(TRANSACTION_AMOUNT);

    mockMvc
        .perform(
            post(String.format("/_/members/%s/charge", TOLVANSSON_MEMBER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest)))
        .andExpect(status().isAccepted());

    val memberEvents =
        eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());
    assertThat(memberEvents, hasEvent(ChargeCreatedEvent.class));

    val trustlyOrderEvents =
        eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
    assertThat(trustlyOrderEvents, hasEvent(PaymentErrorReceivedEvent.class));
  }

  private void mockTrustlyApiResponse(TrustlyApiResponseResult result) {
    val trustlyResultData = new HashMap<String, Object>();
    trustlyResultData.put("orderid", TRUSTLY_ORDER_ID);
    trustlyResultData.put("url", PAYMENT_URL);
    val trustlyResult = new Result();
    trustlyResult.setData(trustlyResultData);
    val trustlyApiResponse = new Response();
    if (result == TrustlyApiResponseResult.SHOULD_SUCCEED) {
      trustlyApiResponse.setResult(trustlyResult);
    } else {
      val error = new Error();
      trustlyApiResponse.setError(error);
    }

    given(signedApi.sendRequest(any())).willReturn(trustlyApiResponse);
  }

  private enum TrustlyApiResponseResult {
    SHOULD_SUCCEED,
    SHOULD_FAIL
  }
}
