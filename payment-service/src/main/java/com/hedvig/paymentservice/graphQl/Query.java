package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.RegisterAccountProcessingStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import javassist.tools.web.BadHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class Query implements GraphQLQueryResolver {

  private static String HEDVIG_TOKEN = "hedvig.token";
  private MemberRepository memberRepository;
  private AccountRegistrationRepository accountRegistrationRepository;

  public Query(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public BankAccount bankAccount(DataFetchingEnvironment env) throws BadHttpRequest {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("GetBankAccountInfo - hedvig.token is missing");
      return null;
    }
    Optional<Member> optionalMember = memberRepository.findById(memberId);
    return optionalMember.map(BankAccount::fromMember).orElse(null);
  }

  //TODO: Catch Red days - Weekends
  public LocalDate chargeDate() {
    return LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 27);
  }

  public RegisterAccountProcessingStatus registerAccountProcessingStatus(String orderId) {
    Optional<AccountRegistration> optionalRegisterAccount = accountRegistrationRepository.findById(UUID.fromString(orderId));
    return optionalRegisterAccount.
      map(accountRegistration -> RegisterAccountProcessingStatus.valueOf(accountRegistration.getStatus().name()))
      .orElse(null);
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
