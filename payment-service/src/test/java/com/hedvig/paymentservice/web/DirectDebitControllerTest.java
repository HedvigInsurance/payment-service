package com.hedvig.paymentservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DirectDebitControllerTest {

    private static final String MEMBER_ID = "12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrustlyService trustlyService;

    @MockBean
    private BankAccountService bankAccountService;

    @Test
    public void Should_ReturnDirectDebitStatus_WhenMemberHasActivePayinStatus() throws Exception {
        given(bankAccountService.getPayinMethodStatus(Mockito.anyString())).willReturn(PayinMethodStatus.ACTIVE);

        mockMvc
            .perform(get("/directDebit/status").header("hedvig.token", MEMBER_ID))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
            .andExpect(jsonPath("$.directDebitActivated").value(true));
    }

    @Test
    public void Should_ReturnDirectDebitStatus_WhenMemberHasNeedsSetupPayinStatus() throws Exception {
        given(bankAccountService.getPayinMethodStatus(Mockito.anyString())).willReturn(PayinMethodStatus.NEEDS_SETUP);

        mockMvc
            .perform(get("/directDebit/status").header("hedvig.token", MEMBER_ID))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
            .andExpect(jsonPath("$.directDebitActivated").value(false));
    }

    @Test
    public void Should_ReturnDirectDebitStatus_WhenMemberHasPendingPayinStatus() throws Exception {
        given(bankAccountService.getPayinMethodStatus(Mockito.anyString())).willReturn(PayinMethodStatus.PENDING);

        mockMvc
            .perform(get("/directDebit/status").header("hedvig.token", MEMBER_ID))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
            .andExpect(jsonPath("$.directDebitActivated").value(false));
    }

    @Test
    public void Should_ReturnOk_WhenMemberRegisterSuccessfullyForDirectDebit() throws Exception {

        given(trustlyService.requestDirectDebitAccount(any(), any(), any()))
            .willReturn(new DirectDebitResponse("url", "orderId"));

        mockMvc
            .perform(post("/directDebit/register")
                .header("hedvig.token", MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterDirectDebitRequestDTO("Tst",
                        "tdst", "198902171234", null))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath(".url").value("url"));
    }

}
