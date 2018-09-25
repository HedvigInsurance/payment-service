package com.hedvig.paymentservice.serviceIntergration.memberService;

import com.hedvig.paymentservice.configuration.FeignConfiguration;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
    name = "memberServiceClient",
    url = "${hedvig.member-service.url:member-service}",
    configuration = FeignConfiguration.class)
public interface MemberServiceClient {

  @RequestMapping(value = "/i/member/{memberId}/sanctionList", method = RequestMethod.GET)
  ResponseEntity<SanctionStatus> getSanctionListStatus(@PathVariable("memberId") String memberId);

  @RequestMapping(value = "/i/member/{memberId}", method = RequestMethod.GET)
  ResponseEntity<Member> getMember(@PathVariable("memberId") String memberId);
}
