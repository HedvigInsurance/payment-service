package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.RegisterAccountProcessingStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Optional;

@Slf4j
@Component
public class Query implements GraphQLQueryResolver {

  private static String HEDVIG_TOKEN = "hedvig.token";
  private MemberRepository memberRepository;
  private AccountRegistrationRepository accountRegistrationRepository;
  private ProductPricingService productPricingService;

  public Query(MemberRepository memberRepository, AccountRegistrationRepository accountRegistrationRepository, ProductPricingService productPricingService) {
    this.memberRepository = memberRepository;
    this.accountRegistrationRepository = accountRegistrationRepository;
    this.productPricingService = productPricingService;
  }

  public BankAccount bankAccount(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("GetBankAccountInfo - hedvig.token is missing");
      return null;
    }
    Optional<Member> optionalMember = memberRepository.findById(memberId);
    return optionalMember.map(BankAccount::fromMember).orElse(null);
  }


  @Deprecated
  public LocalDate chargeDate() {
    return LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 27);
  }

  //TODO: Catch Red days - Weekends
  public LocalDate nextChargeDate(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("registerAccountProcessingStatus - hedvig.token is missing");
      return null;
    }

    Optional<InsuranceStatus> status = productPricingService.getInsuranceStatus(memberId);

    if (!status.isPresent() || status.get() != InsuranceStatus.ACTIVE) {
      return null;
    }

    return LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 27);
  }

  public RegisterAccountProcessingStatus registerAccountProcessingStatus(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("registerAccountProcessingStatus - hedvig.token is missing");
      return null;
    }
    Optional<AccountRegistration> optionalRegisterAccount = accountRegistrationRepository.findByMemberId(memberId).stream().max(Comparator.comparing(AccountRegistration::getInitiated));

    //Hack for fixing App until we can get a release out
    return RegisterAccountProcessingStatus.CONFIRMED;
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
