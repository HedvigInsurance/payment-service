package com.hedvig.paymentservice.graphQl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.RegisterAccountProcessingStatus;
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Component
public class Query implements GraphQLQueryResolver {

  private static String HEDVIG_TOKEN = "hedvig.token";
  private BankAccountService bankAccountService;

  public Query(BankAccountService bankAccountService) {
    this.bankAccountService = bankAccountService;
  }

  public BankAccount bankAccount(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    return bankAccountService.getBankAccount(memberId);
  }

  public LocalDate nextChargeDate(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    return bankAccountService.getNextChargeDate(memberId);
  }

  public DirectDebitStatus directDebitStatus(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    return bankAccountService.getDirectDebitStatus(memberId);
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

  @Deprecated
  public RegisterAccountProcessingStatus registerAccountProcessingStatus(DataFetchingEnvironment env) {
    String memberId = getToken(env);
    if (memberId == null) {
      log.error("registerAccountProcessingStatus - hedvig.token is missing");
      return null;
    }
    //Hack for fixing App until we can get a release out
    return RegisterAccountProcessingStatus.CONFIRMED;
  }

  @Deprecated
  public LocalDate chargeDate() {
    return LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 27);
  }

}
