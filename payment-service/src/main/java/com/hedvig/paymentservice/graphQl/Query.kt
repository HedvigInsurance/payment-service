package com.hedvig.paymentservice.graphQl

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.hedvig.graphql.commons.extensions.getToken
import com.hedvig.graphql.commons.extensions.getTokenOrNull
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.RegisterAccountProcessingStatus
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth

@Component
class Query(
  private val bankAccountService: BankAccountService,
  private val adyenService: AdyenService
) : GraphQLQueryResolver {
  fun bankAccount(env: DataFetchingEnvironment): BankAccount {
    val memberId: String = env.getToken()
    return bankAccountService.getBankAccount(memberId)
  }

  fun nextChargeDate(env: DataFetchingEnvironment): LocalDate {
    val memberId: String = env.getToken()
    return bankAccountService.getNextChargeDate(memberId)
  }

  fun directDebitStatus(env: DataFetchingEnvironment): DirectDebitStatus {
    val memberId: String = env.getToken()
    return bankAccountService.getDirectDebitStatus(memberId)
  }

  fun availablePaymentMethods(
    env: DataFetchingEnvironment
  ): AvailablePaymentMethodsResponse {
    return adyenService.getAvailablePaymentMethods()
  }

  fun activePaymentMethods(
    env: DataFetchingEnvironment
  ): ActivePaymentMethodsResponse {
    return adyenService.getActivePaymentMethods(env.getToken())
  }

  @Deprecated("")
  fun registerAccountProcessingStatus(env: DataFetchingEnvironment): RegisterAccountProcessingStatus? {
    val memberId: String? = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("registerAccountProcessingStatus - hedvig.token is missing")
      return null
    }
    //Hack for fixing App until we can get a release out
    return RegisterAccountProcessingStatus.CONFIRMED
  }

  @Deprecated("")
  fun chargeDate(): LocalDate {
    return LocalDate.of(YearMonth.now().year, YearMonth.now().month, 27)
  }

  companion object {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
  }
}
