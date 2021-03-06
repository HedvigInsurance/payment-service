package com.hedvig.paymentservice.graphQl

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.hedvig.graphql.commons.extensions.getToken
import com.hedvig.graphql.commons.extensions.getTokenOrNull
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.ActivePayoutMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.graphQl.types.RegisterAccountProcessingStatus
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth

@Component
class Query(
    private val bankAccountService: BankAccountService,
    private val adyenService: AdyenService,
    @param:Value("\${hedvig.adyen.allowTrustlyPayouts}")
    private val allowTrustlyPayouts: Boolean,
) : GraphQLQueryResolver {
    fun bankAccount(env: DataFetchingEnvironment): BankAccount? {
        val memberId: String? = env.getTokenOrNull()
        if (memberId == null) {
            logger.error("bankAccount - hedvig.token is missing")
            return null
        }
        return bankAccountService.getBankAccount(memberId)
    }

    fun nextChargeDate(env: DataFetchingEnvironment): LocalDate? {
        val memberId: String = env.getToken()
        return bankAccountService.getNextChargeDate(memberId)
    }

    @Deprecated("replaced by 'payinMethodStatus'")
    fun directDebitStatus(env: DataFetchingEnvironment): DirectDebitStatus {
        val memberId: String? = env.getTokenOrNull()
        if (memberId == null) {
            logger.error("directDebitStatus - hedvig.token is missing")
            return DirectDebitStatus.NEEDS_SETUP
        }
        return bankAccountService.getDirectDebitStatus(memberId)
    }

    fun payinMethodStatus(env: DataFetchingEnvironment): PayinMethodStatus {
        val memberId: String? = env.getTokenOrNull()

        if (memberId == null) {
            logger.error("payinMethodStatus - hedvig.token is missing")
            return PayinMethodStatus.NEEDS_SETUP
        }

        return bankAccountService.getPayinMethodStatus(memberId)
    }

    fun availablePaymentMethods(
        env: DataFetchingEnvironment
    ): AvailablePaymentMethodsResponse {
        val memberId: String = env.getToken()
        return adyenService.getAvailablePayinMethods(memberId)
    }

    fun availablePayoutMethods(
        env: DataFetchingEnvironment
    ): AvailablePaymentMethodsResponse {
        val memberId: String = env.getToken()
        return adyenService.getAvailablePayoutMethods(memberId)
    }

    fun activePaymentMethods(
        env: DataFetchingEnvironment
    ): ActivePaymentMethodsResponse? {
        val memberId: String? = env.getTokenOrNull()
        if (memberId == null) {
            logger.error("activePaymentMethods - hedvig.token is missing")
            return null
        }
        return adyenService.getActivePayinMethods(memberId)
    }

    fun adyenPublicKey(
        env: DataFetchingEnvironment
    ): String = adyenService.fetchAdyenPublicKey()

    fun activePayoutMethods(
        env: DataFetchingEnvironment
    ): ActivePayoutMethodsResponse? {
        val memberId: String? = env.getTokenOrNull()
        if (memberId == null) {
            logger.error("activePayoutMethods - hedvig.token is missing")
            return null
        }

        if (!allowTrustlyPayouts) {
            return null
        }
        val status = adyenService.getLatestPayoutTokenRegistrationStatus(memberId) ?: return null

        return ActivePayoutMethodsResponse(status = status)
    }

    @Deprecated("replaced by  `directDebitStatus`")
    fun registerAccountProcessingStatus(env: DataFetchingEnvironment): RegisterAccountProcessingStatus? {
        val memberId: String? = env.getTokenOrNull()
        if (memberId == null) {
            logger.error("registerAccountProcessingStatus - hedvig.token is missing")
            return null
        }
        // Hack for fixing App until we can get a release out
        return RegisterAccountProcessingStatus.CONFIRMED
    }

    @Deprecated("replaced by `nextChargeDate`")
    fun chargeDate(): LocalDate {
        return LocalDate.of(YearMonth.now().year, YearMonth.now().month, 27)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this.javaClass)!!
    }
}
