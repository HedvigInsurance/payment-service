package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.hedvig.paymentservice.graphQl.types.CancelDirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.DirectDebitResponse;
import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext;
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class Mutation implements GraphQLMutationResolver {

  private TrustlyService trustlyService;
  private MemberService memberService;
  private static String HEDVIG_TOKEN = "hedvig.token";

  public Mutation(TrustlyService trustlyService, MemberService memberService) {
    this.trustlyService = trustlyService;
    this.memberService = memberService;
  }

  public DirectDebitResponse registerDirectDebit(RegisterDirectDebitClientContext clientContext, DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("GetBankAccountInfo - hedvig.token is missing");
      return null;
    }

    Optional<Member> optionalMember = memberService.getMember(memberId);

    if (!optionalMember.isPresent()) {
      return null;
    }

    Member member = optionalMember.get();

    com.hedvig.paymentservice.web.dtos.DirectDebitResponse response =
      trustlyService.requestDirectDebitAccount(
        DirectDebitOrderInfo.Companion.fromMember(member),
        clientContext == null ? null : clientContext.getSuccessUrl(),
        clientContext == null ? null : clientContext.getFailureUrl()
      );

    return DirectDebitResponse.fromDirectDebitResposne(response);
  }

  public CancelDirectDebitStatus cancelDirectDebitRequest(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("GetBankAccountInfo - hedvig.token is missing");
      return CancelDirectDebitStatus.DECLINED_MISSING_TOKEN;
    }
    if (trustlyService.cancelDirectDebitAccountRequest(memberId)) {
      return CancelDirectDebitStatus.ACCEPTED;
    }
    return CancelDirectDebitStatus.DECLINED_MISSING_REQUEST;
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
