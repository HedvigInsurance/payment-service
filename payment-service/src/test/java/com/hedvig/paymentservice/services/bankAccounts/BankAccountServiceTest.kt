package com.hedvig.paymentservice.services.bankAccounts

import com.google.common.collect.Lists
import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus as DirectDebitStatusDTO
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.util.Optional

@RunWith(SpringRunner::class)
class BankAccountServiceTest {

    @MockkBean
    lateinit var memberRepository: MemberRepository

    @MockkBean
    lateinit var accountRegistrationRepository: AccountRegistrationRepository

    @MockkBean
    lateinit var productPricingService: ProductPricingService

    @MockkBean
    lateinit var directDebitAccountOrderRepository: DirectDebitAccountOrderRepository

    lateinit var bankAccountService: BankAccountService

    @Before
    fun setUp() {
        bankAccountService =
            BankAccountServiceImpl(
                memberRepository,
                accountRegistrationRepository,
                productPricingService,
                directDebitAccountOrderRepository
            )
    }

    @Test
    fun When_memberDoesNotExistInPaymentService_Then_Return_NeedSetup() {
        setMockData(
            directDebitStatus = null,
            accountRegistrationStatus = null
        )

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceWuthoutDirectDebitStatus_Then_Return_NeedSetup() {
        setMockData(
            directDebitStatus = null,
            accountRegistrationStatus = null
        )

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsNull_Then_Return_Active() {
        setMockData(directDebitStatus = DirectDebitStatus.CONNECTED, accountRegistrationStatus = null)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInProgress_Then_Return_Pending() {
        setMockData(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.IN_PROGRESS)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.PENDING)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInDone_Then_Return_Active() {
        setMockData(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.CONFIRMED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInCancelled_Then_Return_Active() {
        setMockData(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsNotConnectedAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
        setMockData(DirectDebitStatus.DISCONNECTED, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsPendingAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
        setMockData(DirectDebitStatus.PENDING, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsPendingAndAccountStatusIsInConfirmed_Then_Return_Pending() {
        setMockData(DirectDebitStatus.PENDING, AccountRegistrationStatus.CONFIRMED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.PENDING)
    }

    private fun setMockData(
        directDebitStatus: DirectDebitStatus?,
        accountRegistrationStatus: AccountRegistrationStatus?
    ) {
        every { memberRepository.findById(any()) } returns Optional.of(makeMember(directDebitStatus))
        if (accountRegistrationStatus != null) {
            every { accountRegistrationRepository.findByMemberId(any()) } returns makeAccountRegistration(accountRegistrationStatus)
        }
        else {
            every { accountRegistrationRepository.findByMemberId(any()) } returns emptyList()
        }
    }

    private fun makeMember(
        directDebitStatus: DirectDebitStatus? = DirectDebitStatus.CONNECTED
    ): Member {
        val member = Member()
        member.id = MEMBER_ID
        member.directDebitStatus = directDebitStatus
        return member
    }

    private fun makeAccountRegistration(
        status: AccountRegistrationStatus
    ): List<AccountRegistration> {
        val a = AccountRegistration()
        a.initiated = Instant.now()
        a.memberId = MEMBER_ID
        a.trustlyOrderId = TRUSTLY_ORDER_ID
        a.status = status
        return Lists.newArrayList(a)
    }

    companion object {
        private const val MEMBER_ID = "12345"
        private const val TRUSTLY_ORDER_ID = "RemarkableTrustlyOrderId"
    }
}
