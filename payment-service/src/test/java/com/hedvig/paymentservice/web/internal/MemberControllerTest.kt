package com.hedvig.paymentservice.web.internal

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.PayoutMethodStatus
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService
import com.hedvig.paymentservice.services.payments.PaymentService
import com.hedvig.paymentservice.web.dtos.DirectDebitAccountOrderDTO
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class MemberControllerTest {

    @MockK
    private lateinit var paymentService: PaymentService
    @MockK
    private lateinit var memberRepository: MemberRepository
    @MockK
    private lateinit var bankAccountService: BankAccountService
    @MockK
    private lateinit var adyenService: AdyenService

    @BeforeEach
    internal fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `getPayoutMethodStatus - true if latest adyen payout token status is ACTIVE`() {
        every {
            adyenService.getLatestPayoutTokenRegistrationStatus(any())
        } returns PayoutMethodStatus.ACTIVE

        val cut = create()

        val response = cut.getPayoutMethodStatus("mem").body!!

        assertThat(response.activated).isTrue
    }

    @Test
    fun `getPayoutMethodStatus - true if there is a connected direct debit account order`() {
        every {
            adyenService.getLatestPayoutTokenRegistrationStatus(any())
        } returns PayoutMethodStatus.NEEDS_SETUP
        every {
            bankAccountService.getLatestDirectDebitAccountOrder(any())
        } returns DirectDebitAccountOrderDTO(UUID.randomUUID(), "mid", "aid", DirectDebitStatus.CONNECTED)

        val cut = create()

        val response = cut.getPayoutMethodStatus("mem").body!!

        assertThat(response.activated).isTrue
    }

    @Test
    fun `getPayoutMethodStatus - false if no adyen registration or connected order`() {
        every {
            adyenService.getLatestPayoutTokenRegistrationStatus(any())
        } returns PayoutMethodStatus.NEEDS_SETUP
        every {
            bankAccountService.getLatestDirectDebitAccountOrder(any())
        } returns DirectDebitAccountOrderDTO(UUID.randomUUID(), "mid", "aid", null)

        val cut = create()

        val response = cut.getPayoutMethodStatus("mem").body!!

        assertThat(response.activated).isFalse
    }

    private fun create() = MemberController(
        paymentService, memberRepository, bankAccountService, adyenService
    )
}
