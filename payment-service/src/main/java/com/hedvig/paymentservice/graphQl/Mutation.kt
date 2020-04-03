package com.hedvig.paymentservice.graphQl

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.hedvig.graphql.commons.extensions.getTokenOrNull
import com.hedvig.paymentservice.graphQl.types.AdditionalPaymentsDetailsRequest
import com.hedvig.paymentservice.graphQl.types.AdditionalPaymentsDetailsResponse
import com.hedvig.paymentservice.graphQl.types.CancelDirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.DirectDebitResponse
import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext
import com.hedvig.paymentservice.graphQl.types.TokenizationRequest
import com.hedvig.paymentservice.graphQl.types.TokenizationResponse
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.trustly.TrustlyService
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo.Companion.fromMember
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Mutation(
  private val trustlyService: TrustlyService,
  private val adyenService: AdyenService,
  private val memberService: MemberService
) : GraphQLMutationResolver {

  fun registerDirectDebit(
    clientContext: RegisterDirectDebitClientContext?,
    env: DataFetchingEnvironment
  ): DirectDebitResponse? {
    val memberId = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("RegisterDirectDebit - hedvig.token is missing")
      return null
    }
    val optionalMember =
      memberService.getMember(memberId)
    if (!optionalMember.isPresent) {
      return null
    }
    val member = optionalMember.get()
    val response = trustlyService.requestDirectDebitAccount(
      fromMember(member),
      clientContext?.successUrl,
      clientContext?.failureUrl
    )
    return DirectDebitResponse.fromDirectDebitResposne(response)
  }

  fun tokenizePaymentDetails(
    request: TokenizationRequest,
    env: DataFetchingEnvironment
  ): TokenizationResponse? {
    val memberId = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("tokenizePaymentDetails - hedvig.token is missing")
      return null
    }

    val adyenResponse = adyenService.tokenizePaymentDetails(request, memberId)

    if (adyenResponse.paymentsResponse.action != null) {
      return TokenizationResponse.TokenizationResponseAction(action = adyenResponse.paymentsResponse.action)
    }

    return TokenizationResponse.TokenizationResponseFinished(
      resultCode = adyenResponse.paymentsResponse.resultCode.value
    )
  }

  fun submitAdditionalPaymentDetails(
    request: AdditionalPaymentsDetailsRequest,
    env: DataFetchingEnvironment
  ): AdditionalPaymentsDetailsResponse? {
    val memberId = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("submitAdditionalPaymentDetails - hedvig.token is missing")
      return null
    }

    val adyenResponse = adyenService.submitAdditionalPaymentDetails(request.paymentsDetailsRequest, memberId)

    if (adyenResponse.paymentsResponse.action != null) {
      return AdditionalPaymentsDetailsResponse.AdditionalPaymentsDetailsResponseAction(
        action = adyenResponse.paymentsResponse.action
      )
    }

    return AdditionalPaymentsDetailsResponse.AdditionalPaymentsDetailsResponseFinished(
      resultCode = adyenResponse.paymentsResponse.resultCode.value
    )
  }

  fun cancelDirectDebitRequest(env: DataFetchingEnvironment): CancelDirectDebitStatus {
    val memberId = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("cancelDirectDebitRequest - hedvig.token is missing")
      return CancelDirectDebitStatus.DECLINED_MISSING_TOKEN
    }
    return if (trustlyService.cancelDirectDebitAccountRequest(memberId)) {
      CancelDirectDebitStatus.ACCEPTED
    } else CancelDirectDebitStatus.DECLINED_MISSING_REQUEST
  }

  companion object {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
  }

}
