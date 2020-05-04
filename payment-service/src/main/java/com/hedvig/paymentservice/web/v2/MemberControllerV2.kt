package com.hedvig.paymentservice.web.v2

import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus
import com.hedvig.paymentservice.services.payments.PaymentService
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequestDTO
import com.hedvig.paymentservice.web.dtos.ChargeRequest
import com.hedvig.paymentservice.web.dtos.PaymentProvider
import com.hedvig.paymentservice.web.dtos.PayoutRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping(path = ["/v2/_/members/"])
class MemberControllerV2(
  private val paymentService: PaymentService,
  private val memberService: MemberService,
  private val meerkat: Meerkat
) {

  @PostMapping("{memberId}/charge")
  fun chargeMember(@PathVariable memberId: String, @RequestBody request: ChargeRequest): ResponseEntity<UUID> {
    val result = paymentService.chargeMember(ChargeMemberRequest.fromChargeRequest(memberId, request))

    return if (result.type != ChargeMemberResultType.SUCCESS) {
      ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(result.transactionId)
    } else ResponseEntity.accepted().body(result.transactionId)
  }

  @PostMapping(path = ["{memberId}/payout"])
  fun payoutMember(
    @PathVariable memberId: String,
    @RequestParam(
      name = "category",
      required = false,
      defaultValue = "CLAIM"
    ) category: TransactionCategory,
    @RequestParam(
      name = "referenceId",
      required = false
    ) referenceId: String?,
    @RequestParam(name = "note", required = false) note: String?,
    @RequestParam(name = "handler", required = false) handler: String?,
    @RequestBody request: PayoutRequestDTO
  ): ResponseEntity<UUID> {
    if (category != TransactionCategory.CLAIM &&
      request.amount.number.numberValueExact(BigDecimal::class.java) > BigDecimal.valueOf(10000)
    ) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .build()
    }

    val optionalMember = memberService.getMember(memberId)
    if (!optionalMember.isPresent) {
      return ResponseEntity.notFound().build()
    }

    val member = optionalMember.get()
    val memberStatus = meerkat.getMemberSanctionStatus(member.firstName + ' ' + member.lastName)
    if (memberStatus == SanctionStatus.FullHit) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

    if (!request.sanctionBypassed &&
      (memberStatus == SanctionStatus.Undetermined || memberStatus == SanctionStatus.PartialHit)
    ) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

    val payoutMemberRequest = PayoutMemberRequestDTO(
      request.amount,
      category,
      referenceId,
      note,
      handler
    )
    val result = paymentService.payoutMember(memberId, member, payoutMemberRequest)

    return result.map { uuid -> ResponseEntity.accepted().body(uuid) }.orElseGet {
      ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()
    }
  }

  @GetMapping("connectedToProvider")
  fun getMemberConnectedToProvider(
    @RequestParam paymentProvider: PaymentProvider
  ): ResponseEntity<List<String>> = when(paymentProvider) {
    PaymentProvider.TRUSTLY -> TODO()
    PaymentProvider.ADYEN -> TODO()
    PaymentProvider.ALL -> TODO()
  }

  companion object {
    val logger = LoggerFactory.getLogger(this::class.java)!!
  }

}

