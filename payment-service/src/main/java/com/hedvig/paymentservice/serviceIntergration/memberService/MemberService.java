package com.hedvig.paymentservice.serviceIntergration.memberService;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import java.util.Optional;

public interface MemberService {
  Optional<Member> getMember(String memberId);
}
