package com.hedvig.paymentservice.serviceIntergration.memberService;

import com.hedvig.paymentservice.domain.payments.enums.PayinProvider;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {
  Optional<Member> getMember(String memberId);
  List<String> getMembersByPayinProvider(PayinProvider payinProvider);
}
