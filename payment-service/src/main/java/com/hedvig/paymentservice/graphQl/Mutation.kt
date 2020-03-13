package com.hedvig.paymentservice.graphQl

import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.hedvig.graphql.commons.extensions.getTokenOrNull
import com.hedvig.paymentservice.graphQl.types.CancelDirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.DirectDebitResponse
import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext
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

  fun tokenizeCard(
    paymentRequest: PaymentsRequest,
    env: DataFetchingEnvironment
  ): PaymentsResponse? {
    val memberId = env.getTokenOrNull()
    if (memberId == null) {
      logger.error("registerCard - hedvig.token is missing")
      return null
    }
    val optionalMember =
      memberService.getMember(memberId)
    if (!optionalMember.isPresent) {
      return null
    }
    val member = optionalMember.get()

    return adyenService.tokenizeCard(paymentRequest, member.memberId)
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
