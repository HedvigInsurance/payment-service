package com.hedvig.paymentservice.serviceIntergration.memberService;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Service
public class MemberServiceImpl implements MemberService {

  private final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
  private final MemberServiceClient memberServiceClient;

  public MemberServiceImpl(
      MemberServiceClient memberServiceClient) {
    this.memberServiceClient = memberServiceClient;
  }

  @Override
  public Optional<Member> getMember(String memberId) {
    try {
      ResponseEntity<Member> response = memberServiceClient.getMember(memberId);
      return Optional.of(response.getBody());
    } catch (RestClientResponseException ex) {
      if (ex.getRawStatusCode() == 404) {
        return Optional.empty();
      }
      log.error("Could not find member {} , {}", memberId, ex);
      throw ex;
    }
  }
}
