package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hedvig.paymentservice.graphQl.types.Account;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Query implements GraphQLQueryResolver {

  private MemberRepository memberRepository;

  public Query(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public Account getAccountInfo(String memberId) {
    Optional<Member> optionalMember = memberRepository.findById(memberId);
    return optionalMember.map(Account::fromMember).orElse(null);
  }
}
