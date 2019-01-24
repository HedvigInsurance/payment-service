package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class Query implements GraphQLQueryResolver {

  private static String HEDVIG_TOKEN = "hedvig.token";
  private MemberRepository memberRepository;

  public Query(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public BankAccount getBankAccountInfo(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null){
      log.info("NULL");
      return null;
    }
    Optional<Member> optionalMember = memberRepository.findById(memberId);
    return optionalMember.map(BankAccount::fromMember).orElse(null);
  }

  private String getToken(DataFetchingEnvironment dfe) {
    Object context = dfe.getExecutionContext().getContext();
    if (context instanceof GraphQLContext) {
      return ((GraphQLContext) context).getHttpServletRequest()
        .map(r -> r.getHeader(HEDVIG_TOKEN))
        .orElse(null);
    }

    return null;
  }
}
