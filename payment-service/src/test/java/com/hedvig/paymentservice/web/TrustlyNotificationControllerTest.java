package com.hedvig.paymentservice.web;

import com.google.gson.Gson;
import com.hedvig.paymentService.trustly.NotificationHandler;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreatePaymentOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.PaymentResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.OrderCompletedEvent;
import javax.transaction.Transactional;
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

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.util.UUID;
import java.util.stream.Collectors;

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
    private Gson gson;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private CommandGateway commandGateway;

    @MockBean
    private NotificationHandler notificationHandler;

    @Test
    public void givenAConfirmedTrustlyChargeOrder_whenReceivingNotification_thenShouldReturnOk() throws Exception {
        commandGateway.sendAndWait(new CreatePaymentOrderCommand(
            HEDVIG_ORDER_ID,
            UUID.fromString(TRANSACTION_ID),
            MEMBER_ID,
            TRANSACTION_AMOUNT,
            TRUSTLY_ACCOUNT_ID));
        commandGateway.sendAndWait(new PaymentResponseReceivedCommand(
            HEDVIG_ORDER_ID,
            TRANSACTION_URL,
            TRUSTLY_ORDER_ID
        ));

        val request = createTrustlyCreditNotificationRequest();
        given(notificationHandler.handleNotification(any()))
            .willReturn(request);

        mockMvc
            .perform(
                post("/hooks/trustly/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(request))
            )
            .andExpect(status().isOk());

        val orderEvents = eventStore
            .readEvents(HEDVIG_ORDER_ID.toString())
            .asStream()
            .collect(Collectors.toList());
        assertThat(
            orderEvents
                .stream()
                .filter(e -> e.getPayload() instanceof OrderCompletedEvent)
                .collect(Collectors.toList()),
            not(empty())
        );
    }
}
