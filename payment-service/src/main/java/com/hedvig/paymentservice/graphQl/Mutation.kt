package com.hedvig.paymentservice.graphQl

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.hedvig.paymentservice.graphQl.types.CancelDirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.DirectDebitResponse
import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.trustly.TrustlyService
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo.Companion.fromMember
import graphql.schema.DataFetchingEnvironment
import graphql.servlet.GraphQLContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

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
    val memberId = getToken(env)
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

  fun registerCard(
    env: DataFetchingEnvironment
  ): Any? {
    val memberId = getToken(env)
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
//
//    val response = adyenService.registerToken()

    return null
  }

  fun cancelDirectDebitRequest(env: DataFetchingEnvironment): CancelDirectDebitStatus {
    val memberId = getToken(env)
    if (memberId == null) {
      logger.error("cancelDirectDebitRequest - hedvig.token is missing")
      return CancelDirectDebitStatus.DECLINED_MISSING_TOKEN
    }
    return if (trustlyService.cancelDirectDebitAccountRequest(memberId)) {
      CancelDirectDebitStatus.ACCEPTED
    } else CancelDirectDebitStatus.DECLINED_MISSING_REQUEST
  }

  private fun getToken(dfe: DataFetchingEnvironment): String? {
    val context = dfe.executionContext.context
    return if (context is GraphQLContext) {
      context.httpServletRequest
        .map { r: HttpServletRequest ->
          r.getHeader(
            HEDVIG_TOKEN
          )
        }
        .orElse(null)
    } else null
  }

  companion object {
    val logger = LoggerFactory.getLogger(this.javaClass)!!
    private const val HEDVIG_TOKEN = "hedvig.token"
  }

}
