package com.hedvig.paymentservice.web;

import static com.hedvig.paymentservice.domain.DomainTestUtilities.hasEvent;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.HEDVIG_ORDER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRANSACTION_AMOUNT;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRANSACTION_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRANSACTION_URL;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ACCOUNT_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_IFRAME_URL;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRUSTLY_ORDER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.makeTrustlyAccountNotificationRequest;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.makeTrustlyCreditNotificationRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.hedvig.paymentService.trustly.NotificationHandler;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand;
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.TrustlyAccountCreatedEvent;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.Ignore;
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
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TrustlyNotificationControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private Gson gson;

  @Autowired private EventStore eventStore;

  @Autowired private CommandGateway commandGateway;

  @MockBean private NotificationHandler notificationHandler;

  @Test
  public void givenAConfirmedTrustlyChargeOrder_whenReceivingNotification_thenShouldReturnOk()
      throws Exception {
    commandGateway.sendAndWait(
        new CreatePaymentOrderCommand(
            HEDVIG_ORDER_ID,
            UUID.fromString(TRANSACTION_ID),
            TOLVANSSON_MEMBER_ID,
            TRANSACTION_AMOUNT,
            TRUSTLY_ACCOUNT_ID));
    commandGateway.sendAndWait(
        new PaymentResponseReceivedCommand(HEDVIG_ORDER_ID, TRANSACTION_URL, TRUSTLY_ORDER_ID));

    val request = makeTrustlyCreditNotificationRequest();
    given(notificationHandler.handleNotification(any())).willReturn(request);

    mockMvc
        .perform(
            post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request)))
        .andExpect(status().isOk());

    val orderEvents =
        eventStore.readEvents(HEDVIG_ORDER_ID.toString()).asStream().collect(Collectors.toList());
    assertThat(orderEvents, hasEvent(OrderCompletedEvent.class));
  }

  @Test
  @Ignore("Test not complete yet")
  public void
      givenAnUnconfirmedTrustlyChargeOrder_whenReceivingNotification_thenShouldReturnSomething()
          throws Exception {
    commandGateway.sendAndWait(
        new CreatePaymentOrderCommand(
            HEDVIG_ORDER_ID,
            UUID.fromString(TRANSACTION_ID),
            TOLVANSSON_MEMBER_ID,
            TRANSACTION_AMOUNT,
            TRUSTLY_ACCOUNT_ID));

    val request = makeTrustlyCreditNotificationRequest();
    given(notificationHandler.handleNotification(any())).willReturn(request);

    mockMvc
        .perform(
            post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  public void
      givenAConfirmedTrustlyOrderAndANonExistingMember_whenRecevingNotification_thenShouldReturnOkAndShouldCreateMember()
          throws Exception {
    commandGateway.sendAndWait(new CreateOrderCommand(TOLVANSSON_MEMBER_ID, HEDVIG_ORDER_ID));
    commandGateway.sendAndWait(
        new SelectAccountResponseReceivedCommand(
            HEDVIG_ORDER_ID, TRUSTLY_IFRAME_URL, TRUSTLY_ORDER_ID));

    val request = makeTrustlyAccountNotificationRequest();
    given(notificationHandler.handleNotification(any())).willReturn(request);

    mockMvc
        .perform(
            post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request)))
        .andExpect(status().isOk());

    val memberEvents =
        eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(MemberCreatedEvent.class));
    assertThat(memberEvents, hasEvent(TrustlyAccountCreatedEvent.class));
  }

  @Test
  public void
      givenAConfirmedTrustlyOrderAndAnExistingMember_whenRecevingNotification_thenShouldReturnOkAndShouldNotCreateMember()
          throws Exception {
    commandGateway.sendAndWait(new CreateMemberCommand(TOLVANSSON_MEMBER_ID));
    commandGateway.sendAndWait(new CreateOrderCommand(TOLVANSSON_MEMBER_ID, HEDVIG_ORDER_ID));
    commandGateway.sendAndWait(
        new SelectAccountResponseReceivedCommand(
            HEDVIG_ORDER_ID, TRUSTLY_IFRAME_URL, TRUSTLY_ORDER_ID));

    val request = makeTrustlyAccountNotificationRequest();
    given(notificationHandler.handleNotification(any())).willReturn(request);

    mockMvc
        .perform(
            post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request)))
        .andExpect(status().isOk());

    val memberEvents =
        eventStore.readEvents(TOLVANSSON_MEMBER_ID).asStream().collect(Collectors.toList());

    assertThat(memberEvents, hasEvent(TrustlyAccountCreatedEvent.class));
  }
}
