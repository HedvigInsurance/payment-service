package com.hedvig.paymentservice.serviceIntergration.memberService

import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import java.util.Optional

interface MemberService {
  fun getMember(memberId: String): Optional<Member>
  fun getMembersByPayinProvider(payinProvider: PayinProvider): List<String>
  fun getPickedLocale(memberId: String): String
}
