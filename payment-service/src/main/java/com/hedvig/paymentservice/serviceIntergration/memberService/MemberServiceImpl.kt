package com.hedvig.paymentservice.serviceIntergration.memberService

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.enums.PayinProvider
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import java.util.Optional

@Service
class MemberServiceImpl(
  private val memberServiceClient: MemberServiceClient,
  private val memberRepository: MemberRepository
) : MemberService {

  override fun getMember(memberId: String): Optional<Member> {
    return try {
      val response = memberServiceClient.getMember(memberId)
      Optional.of(response.body!!)
    } catch (ex: RestClientResponseException) {
      if (ex.rawStatusCode == 404) {
        return Optional.empty()
      }
      log.error("Could not find member {} , {}", memberId, ex)
      throw ex
    }
  }

  override fun getPickedLocale(memberId: String): String {
    return memberServiceClient.getPickedLocale(memberId).pickedLocale!!
  }

  override fun getMembersByPayinProvider(payinProvider: PayinProvider): List<String> {
    val members = memberRepository.findAll()

    return when (payinProvider) {
      PayinProvider.TRUSTLY -> {
        members.filter {
          it.trustlyAccountNumber != null
            && it.directDebitStatus == DirectDebitStatus.CONNECTED
        }
      }
      PayinProvider.ADYEN -> {
        members.filter { it.adyenRecurringDetailReference != null }
      }
    }.map { it.id }
  }

    override fun membersWithConnectedPayinMethodForMarket(market: Market): List<String> {
        val members = memberRepository.findAll()

        val membersWithConnectedPayinMethod = members.filter { member ->
            when (market) {
                Market.NORWAY,
                Market.DENMARK -> member.adyenRecurringDetailReference != null
                Market.SWEDEN -> member.directDebitStatus == DirectDebitStatus.CONNECTED
                    && member.trustlyAccountNumber != null
            }
        }

        return membersWithConnectedPayinMethod.map { member -> member.id }
    }

  companion object {
    private val log = LoggerFactory.getLogger(MemberServiceImpl::class.java)
  }
}
