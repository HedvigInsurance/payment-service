package com.hedvig.paymentservice.web.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.services.exceptions.OrderNotFoundException;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation;
import com.hedvig.paymentservice.trustly.testHelpers.TestData;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import java.util.Objects;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@WebMvcTest(controllers = TrustlyController.class)
public class TrustlyControllerTest {

  public static final String TRUSTLY_IFRAME_URL = "https://example.url";
  public static final String IFRAME_URL = "http://alkjdljda";
  public static final OrderState CONFIRMED = OrderState.CONFIRMED;
  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @MockBean TrustlyService trustlyService;
  public static final UUID ORDER_ID = UUID.randomUUID();

  @Test
  public void getTrustlyDirectDebitReturnsEmptyDTO() throws Exception {

    mockMvc
        .perform(get("/_/trustlyOrder/registerDirectDebit"))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void post_registerDirectDebitCallsTrustlyService() throws Exception {

    DirectDebitRequest requestData = TestData.makeDirectDebitRequest();

    given(trustlyService.requestDirectDebitAccount(any()))
        .willReturn(new DirectDebitResponse(TRUSTLY_IFRAME_URL, ORDER_ID.toString()));

    mockMvc
        .perform(
            post("/_/trustlyOrder/registerDirectDebit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.url").value(TRUSTLY_IFRAME_URL));
  }

  @Test
  public void post_returns404_if_memberNotFound() throws Exception {

    DirectDebitRequest requestData = TestData.makeDirectDebitRequest();

    given(trustlyService.requestDirectDebitAccount(any())).willThrow(OrderNotFoundException.class);

    mockMvc
        .perform(
            post("/_/trustlyOrder/" + ORDER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void post_returnsOrderInformation() throws Exception {
    given(trustlyService.orderInformation(ORDER_ID))
        .willReturn(new OrderInformation(ORDER_ID, IFRAME_URL, OrderState.CONFIRMED));

    mockMvc
        .perform(get("/_/trustlyOrder/" + ORDER_ID))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.iframeUrl").value(IFRAME_URL))
        .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
        .andExpect(jsonPath("$.state").value(Objects.toString(CONFIRMED)));
  }
}
