package com.hedvig.paymentservice.web.internal;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.exceptions.MemberNotFoundException;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation;
import com.hedvig.paymentservice.trustly.testHelpers.TestData;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TrustlyController.class)
public class TrustlyControllerTest {

    public static final String TRUSTLY_IFRAME_URL = "https://example.url";
    public static final String IFRAME_URL = "http://alkjdljda";
    public static final OrderState CONFIRMED = OrderState.CONFIRMED;
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TrustlyService trustlyService;


    @Test
    public void getTrustlyDirectDebitReturnsEmptyDTO() throws Exception {

        mockMvc.perform(
                get("/_/trustlyOrder/registerDirectDebit")
        ).andExpect(status().is2xxSuccessful());

    }

    @Test
    public void post_registerDirectDebitCallsTrustlyService() throws Exception {

        DirectDebitRequest requestData = TestData.createDirectDebitRequest();

        given(trustlyService.requestDirectDebitAccount(any())).willReturn(new DirectDebitResponse(TRUSTLY_IFRAME_URL));

        mockMvc.perform(
                post("/_/trustlyOrder/registerDirectDebit").
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(requestData))
        ).andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.url").value(TRUSTLY_IFRAME_URL));
    }

    @Test
    public void post_returns404_if_memberNotFound() throws Exception {

        DirectDebitRequest requestData = TestData.createDirectDebitRequest();

        given(trustlyService.requestDirectDebitAccount(any())).willThrow(MemberNotFoundException.class);

        mockMvc.perform(
                post("/_/trustlyOrder/registerDirectDebit").
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(requestData))
        ).andExpect(status().is4xxClientError());
    }


    @Test
    public void post_returnsOrderInformation() throws Exception {
        UUID requestId = UUID.randomUUID();
        given(trustlyService.orderInformation(requestId)).willReturn(new OrderInformation(requestId, IFRAME_URL, OrderState.CONFIRMED));

        mockMvc.perform(
                    get("/_/trustlyOrder/"+requestId)).
                andExpect(status().is2xxSuccessful()).
                andExpect(jsonPath("$.iframeUrl").value(IFRAME_URL)).
                andExpect(jsonPath("$.id").value(requestId.toString())).
                andExpect(jsonPath("$.state").value(Objects.toString(CONFIRMED)));

    }

}