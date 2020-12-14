package com.hedvig.paymentservice.serviceIntergration.memberService

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import java.util.Optional

@Service
class MemberServiceImpl(
    private val memberServiceClient: MemberServiceClient
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

    companion object {
        private val log = LoggerFactory.getLogger(MemberServiceImpl::class.java)
    }
}
