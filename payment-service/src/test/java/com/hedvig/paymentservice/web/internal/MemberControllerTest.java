package com.hedvig.paymentservice.web.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MemberController.class)
public class MemberControllerTest {
    @Autowired
    MockMvc mockMvc;

    private static final String memberId = "1";

    @Test
    public void chargeMemberReturnsAccepted() throws Exception {
        // TODO: Any mocks necessary?
        mockMvc
            .perform(post(String.format("/_/members/%s/charge", memberId)))
            .andExpect(status().is(202));

    }
}
