package com.hedvig.paymentservice.services.payments

import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.domain.payments.TransactionCategory
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand
import com.hedvig.paymentservice.domain.payments.commands.CreateMemberCommand
import com.hedvig.paymentservice.domain.payments.commands.CreatePayoutCommand
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand
import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus
import com.hedvig.paymentservice.services.Helpers
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType
import com.hedvig.paymentservice.services.payments.exception.PayoutFailedException
import com.hedvig.paymentservice.web.dtos.PayoutRequestDTO
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Service
class PaymentService(
    private val commandGateway: CommandGateway,
    private val uuidGenerator: UUIDGenerator,
    private val memberService: MemberService,
    private val meerkat: Meerkat
) {
    fun createMember(memberId: String?) {
        commandGateway.sendAndWait<Any>(CreateMemberCommand(memberId))
    }

    fun chargeMember(request: ChargeMemberRequest): ChargeMemberResult {
        val transactionId = uuidGenerator.generateRandom()
        return try {
            commandGateway.sendAndWait(
                CreateChargeCommand(
                    request.memberId,
                    transactionId,
                    request.amount,
                    Instant.now(),
                    Helpers.createTrustlyInboxfromMemberId(request.memberId),
                    request.createdBy
                ))
        } catch (exception: AggregateNotFoundException) {
            logger.error("No aggregate found for member" + request.memberId + "assume member has not connected their direct debit or card")
            ChargeMemberResult(
                transactionId,
                ChargeMemberResultType.NO_PAYIN_METHOD_FOUND
            )
        }
    }

    fun payoutMember(memberId: String, request: PayoutRequestDTO): Optional<UUID> {
        if (request.category != TransactionCategory.CLAIM &&
            request.amount.number.numberValueExact(BigDecimal::class.java) > BigDecimal.valueOf(10000)
        ) {
            throw PayoutFailedException("Non claim category, amount exceeds 10.000", HttpStatus.BAD_REQUEST)
        }

        val optionalMember = memberService.getMember(memberId)

        if (!optionalMember.isPresent) {
            throw PayoutFailedException("Member not found", HttpStatus.NOT_FOUND)
        }

        val member = optionalMember.get()
        val memberStatus = meerkat.getMemberSanctionStatus(member.firstName + ' ' + member.lastName)
        if (memberStatus == SanctionStatus.FullHit) {
            throw PayoutFailedException("Member sanction status full hit", HttpStatus.FORBIDDEN)
        }

        if (!request.sanctionBypassed &&
            (memberStatus == SanctionStatus.Undetermined || memberStatus == SanctionStatus.PartialHit)
        ) {
            throw PayoutFailedException("Member sanction status partial hit", HttpStatus.FORBIDDEN)
        }

        val transactionId = uuidGenerator.generateRandom()
        val result = commandGateway.sendAndWait<Boolean>(
            CreatePayoutCommand(
                memberId,
                member.street + " " + member.city + " " + member.zipCode,
                member.country,
                member.birthDate,
                member.firstName,
                member.lastName,
                transactionId,
                request.amount,
                Instant.now(),
                request.category!!,
                request.referenceId,
                request.note,
                request.handler,
                member.email,
                request.carrier
            )
        )
        return if (result) Optional.of(transactionId) else Optional.empty()
    }

    fun sendCommand(cmd: UpdateTrustlyAccountCommand?) {
        commandGateway.sendAndWait<Any>(cmd)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PaymentService::class.java)
    }

}

