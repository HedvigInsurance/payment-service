package com.hedvig.paymentservice.web.v2

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.enums.Carrier
import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import com.hedvig.paymentservice.services.payinMethodFilter.MemberPayinMethodFilterService
import com.hedvig.paymentservice.services.payments.PaymentService
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequestDTO
import com.hedvig.paymentservice.web.dtos.ChargeRequest
import com.hedvig.paymentservice.web.dtos.PayoutRequestDTO
import java.math.BigDecimal
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/v2/_/members/"])
class MemberControllerV2(
    private val paymentService: PaymentService,
    private val memberPayinMethodFilterService: MemberPayinMethodFilterService
) {

    @PostMapping("{memberId}/charge")
    fun chargeMember(@PathVariable memberId: String, @RequestBody request: ChargeRequest): ResponseEntity<UUID> {
        val result = paymentService.chargeMember(ChargeMemberRequest.fromChargeRequest(memberId, request))

        return if (result.type != ChargeMemberResultType.SUCCESS) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(result.transactionId)
        } else ResponseEntity.accepted().body(result.transactionId)
    }

    @PostMapping("{memberId}/autocharge")
    fun chargeMemberAutomatically(
        @PathVariable memberId: String,
        @RequestBody request: ChargeRequest
    ): ResponseEntity<ChargeMemberResult> {
        val result = paymentService.chargeMember(ChargeMemberRequest.fromChargeRequest(memberId, request))
        return ResponseEntity.ok(result)
    }


    @PostMapping(path = ["{memberId}/payout"])
    fun payoutMember(
        @PathVariable memberId: String,
        @RequestParam(required = false, defaultValue = "CLAIM") category: TransactionCategory, // Deprecated use request
        @RequestParam(required = false) referenceId: String?, // Deprecated use request
        @RequestParam(name = "note", required = false) note: String?, // Deprecated use request
        @RequestParam(name = "handler", required = false) handler: String?, // Deprecated use request
        @RequestParam(required = false) carrier: Carrier?, // Deprecated use request
        @RequestBody request: PayoutRequestDTO
    ): ResponseEntity<UUID> {
        // Yes this is a bit messy but let's stop using RequestParam
        val payoutRequest = request.copy(
            category = request.category ?: category,
            referenceId = request.referenceId ?: referenceId,
            note = request.note ?: note,
            handler = request.handler ?: handler,
            carrier = request.carrier ?: carrier ?: if (category == TransactionCategory.CLAIM) Carrier.HDI else null // TODO: FIXME remove this logic once carrier is sent from claims-service
        )

        val result = paymentService.payoutMember(memberId, payoutRequest)

        return result.map { uuid -> ResponseEntity.accepted().body(uuid) }.orElseGet {
            ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()
        }
    }

    @PostMapping("/connectedPayinProviders/markets/{market}")
    fun getMembersWithConnectedPayinMethodForMarket(
        @PathVariable market: Market,
        @RequestBody memberIds: List<String>
    ): ResponseEntity<List<String>> =
        ResponseEntity.ok(
            memberPayinMethodFilterService.membersWithConnectedPayinMethodForMarket(memberIds, market)
        )

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
