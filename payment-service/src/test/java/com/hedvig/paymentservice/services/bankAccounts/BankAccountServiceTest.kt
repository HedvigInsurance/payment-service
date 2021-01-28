package com.hedvig.paymentservice.services.bankAccounts

import com.google.common.collect.Lists
import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus
import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.query.adyenAccount.AdyenAccount
import com.hedvig.paymentservice.query.adyenAccount.AdyenAccountRepository
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrderRepository
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
import java.time.temporal.ChronoUnit
import java.util.*
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus as DirectDebitStatusDTO

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

    @MockkBean
    lateinit var adyenAccountRepository: AdyenAccountRepository

    lateinit var bankAccountService: BankAccountService

    @Before
    fun setUp() {
        bankAccountService =
            BankAccountServiceImpl(
                memberRepository,
                accountRegistrationRepository,
                productPricingService,
                directDebitAccountOrderRepository,
                adyenAccountRepository
            )
    }

    @Test
    fun When_memberDoesNotExistInPaymentService_Then_Return_NeedSetup() {
        makeStub(
            directDebitStatus = null,
            accountRegistrationStatus = null
        )

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceWuthoutDirectDebitStatus_Then_Return_NeedSetup() {
        makeStub(
            directDebitStatus = null,
            accountRegistrationStatus = null
        )

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsNull_Then_Return_Active() {
        makeStub(directDebitStatus = DirectDebitStatus.CONNECTED, accountRegistrationStatus = null)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInProgress_Then_Return_Pending() {
        makeStub(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.IN_PROGRESS)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.PENDING)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInDone_Then_Return_Active() {
        makeStub(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.CONFIRMED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInCancelled_Then_Return_Active() {
        makeStub(DirectDebitStatus.CONNECTED, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.ACTIVE)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsNotConnectedAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
        makeStub(DirectDebitStatus.DISCONNECTED, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsPendingAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
        makeStub(DirectDebitStatus.PENDING, AccountRegistrationStatus.CANCELLED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.NEEDS_SETUP)
    }

    @Test
    fun When_memberExistInPaymentServiceAndDirectDebitStatusIsPendingAndAccountStatusIsInConfirmed_Then_Return_Pending() {
        makeStub(DirectDebitStatus.PENDING, AccountRegistrationStatus.CONFIRMED)

        assertThat(bankAccountService.getDirectDebitStatus(MEMBER_ID)).isEqualTo(DirectDebitStatusDTO.PENDING)
    }

    @Test
    fun `when no directDebitAccountOrder exists, expect null as the bankaccount`() {
        every { directDebitAccountOrderRepository.findAllByMemberId(any()) } returns emptyList()

        assertThat(bankAccountService.getBankAccount(MEMBER_ID)).isNull()
    }

    @Test
    fun `when three directDebitAccountOrder exist, expect the latest bankaccount`() {
        val directDebitAccountOrder = makeDirectDebitAccountOrder(Instant.now().minus(7, ChronoUnit.DAYS))
        val directDebitAccountOrderLatest = makeDirectDebitAccountOrder(Instant.now())
        val directDebitAccountOrderTwo = makeDirectDebitAccountOrder(Instant.now().minus(4, ChronoUnit.DAYS))

        every { directDebitAccountOrderRepository.findAllByMemberId(any()) } returns listOf(
            directDebitAccountOrderTwo,
            directDebitAccountOrderLatest,
            directDebitAccountOrder
        )

        assertThat(bankAccountService.getBankAccount(MEMBER_ID))
            .isEqualTo(BankAccount.fromDirectDebitAccountOrder(directDebitAccountOrderLatest))
    }

    @Test
    fun `when neither adyen account or trustly account exists, expect PayinMethodStatus to be NEEDS_SETUP`() {
        every { adyenAccountRepository.findById(any()) } returns Optional.empty()
        every { accountRegistrationRepository.findByMemberId(any()) } returns emptyList()
        every { directDebitAccountOrderRepository.findAllByMemberId(any()) } returns emptyList()

        assertThat(bankAccountService.getPayinMethodStatus(MEMBER_ID))
            .isEqualTo(PayinMethodStatus.NEEDS_SETUP)
    }

    @Test
    fun `when a member exists, and payin provider is adyen, expect PayinMethodStatus to be ACTIVE`() {
        val account = AdyenAccount(MEMBER_ID, "account")
        account.recurringDetailReference = "reference"
        account.accountStatus = AdyenAccountStatus.AUTHORISED

        every { adyenAccountRepository.findById(any()) } returns Optional.of(
            account
        )

        assertThat(bankAccountService.getPayinMethodStatus(MEMBER_ID))
            .isEqualTo(PayinMethodStatus.ACTIVE)
    }

    @Test
    fun `when a member exists, and payin provider is trustly, and latest order payment is PENDING expect PayinMethodStatus to be PENDING`() {
        every { adyenAccountRepository.findById(any()) } returns Optional.empty()

        makeStub(
            directDebitStatus = DirectDebitStatus.PENDING,
            accountRegistrationStatus = AccountRegistrationStatus.IN_PROGRESS
        )

        assertThat(bankAccountService.getPayinMethodStatus(MEMBER_ID))
            .isEqualTo(PayinMethodStatus.PENDING)
    }

    private fun makeStub(
        directDebitStatus: DirectDebitStatus?,
        accountRegistrationStatus: AccountRegistrationStatus?
    ) {
        if (directDebitStatus != null) {
            every { directDebitAccountOrderRepository.findAllByMemberId(any()) } returns listOf(
                makeDirectDebitAccountOrder(
                    status = directDebitStatus
                )
            )
        } else {
            every { directDebitAccountOrderRepository.findAllByMemberId(any()) } returns emptyList()
        }

        if (accountRegistrationStatus != null) {
            every { accountRegistrationRepository.findByMemberId(any()) } returns makeAccountRegistration(
                accountRegistrationStatus
            )
        } else {
            every { accountRegistrationRepository.findByMemberId(any()) } returns emptyList()
        }
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

    private fun makeDirectDebitAccountOrder(
        createdAt: Instant = Instant.now(),
        status: DirectDebitStatus = DirectDebitStatus.CONNECTED
    ) = DirectDebitAccountOrder(
        UUID.randomUUID(),
        MEMBER_ID,
        TRUSTLY_ORDER_ID,
        "Bank",
        "**1234",
        status,
        createdAt
    )

    companion object {
        private const val MEMBER_ID = "12345"
        private const val TRUSTLY_ORDER_ID = "RemarkableTrustlyOrderId"
    }
}
