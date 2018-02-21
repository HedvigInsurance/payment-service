package com.hedvig.paymentservice.web.internal;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.exceptions.MemberNotFoundException;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
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

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TrustlyController.class)
public class TrustlyControllerTest {

    public static final String TRUSTLY_IFRAME_URL = "https://example.url";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TrustlyService trustlyService;


    @Test
    public void getTrustlyDirectDebitReturnsEmptyDTO() throws Exception {

        mockMvc.perform(
                get("/_/member/1337/trustly/registerDirectDebit")
        ).andExpect(status().is2xxSuccessful());

    }

    @Test
    public void post_registerDirectDebitCallsTrustlyService() throws Exception {

        DirectDebitRequest requestData = TestData.createDirectDebitRequest();

        given(trustlyService.requestDirectDebitAccount(anyString(), any())).willReturn(new DirectDebitResponse(TRUSTLY_IFRAME_URL));

        mockMvc.perform(
                post("/_/member/1337/trustly/registerDirectDebit").
                contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(requestData))
        ).andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.url").value(TRUSTLY_IFRAME_URL));
    }

    @Test
    public void post_returns404_if_memberNotFound() throws Exception {

        DirectDebitRequest requestData = TestData.createDirectDebitRequest();

        given(trustlyService.requestDirectDebitAccount(anyString(), any())).willThrow(MemberNotFoundException.class);

        mockMvc.perform(
                post("/_/member/1337/trustly/registerDirectDebit").
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(requestData))
        ).andExpect(status().is4xxClientError());
    }

}