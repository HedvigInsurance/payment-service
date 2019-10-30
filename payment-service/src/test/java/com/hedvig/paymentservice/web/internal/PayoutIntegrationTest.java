package com.hedvig.paymentservice.web.internal;

import static com.hedvig.paymentservice.domain.DomainTestUtilities.hasEvent;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreationFailedEvent;
import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import com.hedvig.paymentservice.web.dtos.PayoutRequestDTO;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.Before;
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

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

  @MockBean
  private MemberService memberService;

  @MockBean
  private Meerkat meerkat;

  @Before
  public void setup() {
    commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));
    given(memberService.getMember(TOLVANSSON_MEMBER_ID)).willReturn(Optional.of(new Member(
      TOLVANSSON_MEMBER_ID,
      TOLVAN_FIRST_NAME,
      TOLVANSSON_LAST_NAME,
      TOLVANSSON_DATE_OF_BIRTH,
      TOLVANSSON_STREET,
      TOLVANSSON_CITY,
      TOLVANSSON_ZIP,
      TOLVANSSON_COUNTRY,
      TOLVANSSON_SSN
    )));

    given(meerkat.getMemberSanctionStatus(TOLVAN_FIRST_NAME + ' ' + TOLVANSSON_LAST_NAME))
      .willReturn(SanctionStatus.NoHit);
    given(uuidGenerator.generateRandom()).willReturn(HEDVIG_ORDER_ID);
  }

  @Test
  public void givenMemberWithoutTrustlyAccount_WhenCreatingPayout_ThenShouldReturnNotAcceptable()
    throws Exception {

    val payoutRequest = new PayoutRequestDTO(TRANSACTION_AMOUNT, true);

    mockMvc
      .perform(
        post(String.format("/v2/_/members/%s/payout", TOLVANSSON_MEMBER_ID))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payoutRequest)))
      .andExpect(status().isNotAcceptable());

    val memberEvents = eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(PayoutCreationFailedEvent.class));
  }

  @Test
  public void
  givenMemberWithTrustlyAccountAndNoProvidedCategory_WhenCreatingPayoutAndTrustlyReturnsSuccess_ThenShouldReturnAcceptedWithClaimCategory()
    throws Exception {

    updateTrustly();

    val payoutRequest = new PayoutRequestDTO(TRANSACTION_AMOUNT, true);

    mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_SUCCEED);

    mockMvc
      .perform(
        post(String.format("/v2/_/members/%s/payout", TOLVANSSON_MEMBER_ID))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payoutRequest)))
      .andExpect(status().isAccepted());

    val memberEvents = eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(PayoutCreatedEvent.class));
    assertThat(memberEvents, hasEvent(PayoutCompletedEvent.class));
    String category = getCategoryFromEventStore(eventStore);
    Assertions.assertThat(category).isEqualTo(TransactionCategory.CLAIM.name());
  }

  @Test
  public void
  givenMemberWithTrustlyAccount_WhenCreatingPayoutAndTrustlyReturnsError_ThenShouldReturnAccepted()
    throws Exception {

    updateTrustly();

    val payoutRequest =
      new PayoutRequestDTO(TRANSACTION_AMOUNT, true);

    mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_FAIL);

    mockMvc
      .perform(
        post(String.format("/v2/_/members/%s/payout", TOLVANSSON_MEMBER_ID))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payoutRequest)))
      .andExpect(status().isAccepted());

    val memberEvents = eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(PayoutCreatedEvent.class));
    assertThat(memberEvents, not(hasEvent(PayoutCompletedEvent.class)));
  }

  @Test
  public void givenPayoutWithIncorrectCategory_WhenCreatingPayout_ThenShouldReturnBadRequest()
    throws Exception {
    val payoutRequest = new PayoutRequestDTO(TRANSACTION_AMOUNT, true);

    mockMvc
      .perform(
        post(String.format("/v2/_/members/%s/payout?category=SOMETHINGTHATISNOTACATEGORY", TOLVANSSON_MEMBER_ID))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payoutRequest)))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void givenPayoutWithMarketingCategory_WhenCreatingPayout_ThenShouldContainMarketingCategory()
    throws Exception {

    updateTrustly();

    val payoutRequest = new PayoutRequestDTO(TRANSACTION_AMOUNT, true);

    mockTrustlyApiResponse(TrustlyApiResponseResult.SHOULD_SUCCEED);

    mockMvc
      .perform(
        post(String.format("/v2/_/members/%s/payout?category=MARKETING", TOLVANSSON_MEMBER_ID))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payoutRequest)))
      .andExpect(status().isAccepted());

    val memberEvents = eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(PayoutCreatedEvent.class));
    assertThat(memberEvents, hasEvent(PayoutCompletedEvent.class));


    val category = getCategoryFromEventStore(eventStore);
    Assertions.assertThat(category).isEqualTo(TransactionCategory.MARKETING.name());
  }

  private void updateTrustly() {

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
        TOLVANSSON_ZIP)
    );
  }

  private String getCategoryFromEventStore(EventStore eventStore) {
    val payoutCreatedEvent = eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream()
      .filter(event -> event.getPayloadType().getTypeName().equalsIgnoreCase(PayoutCreatedEvent.class.getTypeName()))
      .map(event -> (PayoutCreatedEvent) event.getPayload())
      .findFirst();
    return payoutCreatedEvent.get().getCategory().name();
  }

  private void mockTrustlyApiResponse(TrustlyApiResponseResult result) {
    val trustlyResultData = new HashMap<String, Object>();
    trustlyResultData.put("orderid", TRUSTLY_ORDER_ID);

    val trustlyResult = new Result();
    trustlyResult.setData(trustlyResultData);
    val trustlyApiResponse = new Response();

    if (result == TrustlyApiResponseResult.SHOULD_SUCCEED) {
      trustlyApiResponse.setResult(trustlyResult);
    } else {
      val error = new Error();
      trustlyApiResponse.setError(error);
    }

    given(signedApi.sendRequest(any(), anyBoolean())).willReturn(trustlyApiResponse);
  }

  private enum TrustlyApiResponseResult {
    SHOULD_SUCCEED,
    SHOULD_FAIL
  }
}
