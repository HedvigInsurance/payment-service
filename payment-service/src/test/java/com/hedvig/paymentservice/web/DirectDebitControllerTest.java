package com.hedvig.paymentservice.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.paymentservice.PaymentServiceTestConfiguration;
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PaymentServiceTestConfiguration.class)
@WebMvcTest(controllers = DirectDebitController.class)
public class DirectDebitControllerTest {

  private static final String MEMBER_ID = "12345";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private TrustlyService trustlyService;

  @MockBean
  private MemberRepository memberRepository;

  @Test
  public void Should_ReturnBadRequest_WhenMemberCannotBeFound() throws Exception {

    given(memberRepository.findById(Mockito.anyString())).willReturn(Optional.empty());

    mockMvc
        .perform(get("/directDebit/status").header("hedvig.token", MEMBER_ID))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void Should_ReturnDirectDebitStatus_WhenMemberHasDirectDebit() throws Exception {

    given(memberRepository.findById(Mockito.anyString()))
        .willReturn(Optional.of(makeMember(MEMBER_ID, true)));

    mockMvc
        .perform(get("/directDebit/status").header("hedvig.token", MEMBER_ID))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
        .andExpect(jsonPath("$.directDebitActivated").value(true));
  }

  @Test
  public void Should_ReturnOk_WhenMemberRegisterSuccessfullyForDirectDebit() throws Exception {

    given(trustlyService.requestDirectDebitAccount(any()))
        .willReturn(new DirectDebitResponse("url", "orderId"));

    mockMvc
        .perform(post("/directDebit/register")
            .header("hedvig.token", MEMBER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new RegisterDirectDebitRequestDTO("Tst",
                    "tdst", "198902171234"))))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath(".url").value("url"));
  }


  private Member makeMember(String memberId, boolean isConnected) {
    Member member = new Member();
    member.setId(memberId);
    member.setDirectDebitStatus(isConnected ? DirectDebitStatus.CONNECTED : DirectDebitStatus.DISCONNECTED );

    return member;
  }

}
