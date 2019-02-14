package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.hedvig.paymentservice.graphQl.types.DirectDebitResponse;
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Mutation implements GraphQLMutationResolver {

  private TrustlyService trustlyService;
  private MemberService memberService;

  public Mutation(TrustlyService trustlyService, MemberService memberService) {
    this.trustlyService = trustlyService;
    this.memberService = memberService;
  }

  public DirectDebitResponse registerDirectDebit(String memberId) {

    Optional<Member> optionalMember = memberService.getMember(memberId);

    if (!optionalMember.isPresent()) {
      return null;
    }

    Member member = optionalMember.get();

    com.hedvig.paymentservice.web.dtos.DirectDebitResponse response =
      trustlyService.requestDirectDebitAccount(DirectDebitOrderInfo.Companion.fromMember(member));

    return DirectDebitResponse.fromDirectDebitResposne(response);
  }
}
