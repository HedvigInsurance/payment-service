package com.hedvig.paymentservice.serviceIntergration.memberService;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import com.hedvig.paymentservice.web.dtos.PaymentProvider;

import java.util.List;
import java.util.Optional;

public interface MemberService {
  Optional<Member> getMember(String memberId);
  List<String> getMembersConnectedToProvider(PaymentProvider paymentProvider);
}
