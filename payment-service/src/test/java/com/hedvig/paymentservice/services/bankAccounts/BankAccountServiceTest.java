package com.hedvig.paymentservice.services.bankAccounts;

import com.google.common.collect.Lists;
import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus;
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BankAccountServiceTest {

  private static String MEMBER_ID = "12345";
  private static UUID ACCOUNT_REGISTRATION_ID = UUID.fromString("1271d9a4-3e61-11e9-b753-47490be9f5e7");
  private static UUID HEDVIG_ORDER_ID = UUID.fromString("35a15792-3e61-11e9-909b-5f7b645264a6");
  private static String TRUSTLY_ORDER_ID = "RemarkableTrustlyOrderId";

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private AccountRegistrationRepository accountRegistrationRepository;
  @Mock
  private ProductPricingService productPricingService;

  private BankAccountService bankAccountService;

  @Before
  public void setUp() {
    bankAccountService = new BankAccountServiceImpl(memberRepository, accountRegistrationRepository, productPricingService);
  }

  @Test
  public void When_memberDoesNotExistInPaymentService_Then_Return_NeedSetup() {
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }

  @Test
  public void When_memberExistInPaymentServiceWuthoutDirectDebitStatus_Then_Return_NeedSetup() {
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(makeMember(MEMBER_ID)));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsNull_Then_Return_Active() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.CONNECTED);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.ACTIVE);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInProgress_Then_Return_Pending() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.CONNECTED);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    Mockito.when(accountRegistrationRepository.findByMemberId(Mockito.anyString())).thenReturn(makeAccountRegistration(MEMBER_ID, AccountRegistrationStatus.IN_PROGRESS));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.PENDING);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInDone_Then_Return_Active() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.CONNECTED);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    Mockito.when(accountRegistrationRepository.findByMemberId(Mockito.anyString())).thenReturn(makeAccountRegistration(MEMBER_ID, AccountRegistrationStatus.CONFIRMED));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.ACTIVE);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsConnectedAndAccountStatusIsInCancelled_Then_Return_Active() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.CONNECTED);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    Mockito.when(accountRegistrationRepository.findByMemberId(Mockito.anyString())).thenReturn(makeAccountRegistration(MEMBER_ID, AccountRegistrationStatus.CANCELLED));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.ACTIVE);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsNotConnectedAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.DISCONNECTED);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    Mockito.when(accountRegistrationRepository.findByMemberId(Mockito.anyString())).thenReturn(makeAccountRegistration(MEMBER_ID, AccountRegistrationStatus.CANCELLED));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }

  @Test
  public void When_memberExistInPaymentServiceAndDirectDebitStatusIsPendingAndAccountStatusIsInCancelled_Then_Return_Need_Setup() {
    Member m = makeMember(MEMBER_ID);
    m.setDirectDebitStatus(com.hedvig.paymentservice.domain.payments.DirectDebitStatus.PENDING);
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(m));

    Mockito.when(accountRegistrationRepository.findByMemberId(Mockito.anyString())).thenReturn(makeAccountRegistration(MEMBER_ID, AccountRegistrationStatus.CANCELLED));

    assertThat(bankAccountService.getDirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }
  
  private Member makeMember(String memberId) {
    Member member = new Member();
    member.setId(memberId);
    return member;
  }

  private List<AccountRegistration> makeAccountRegistration(String memberId, AccountRegistrationStatus status) {
    AccountRegistration a = new AccountRegistration();
    a.setAccountRegistrationId(ACCOUNT_REGISTRATION_ID);
    a.setHedvigOrderId(HEDVIG_ORDER_ID);
    a.setInitiated(Instant.now());
    a.setMemberId(memberId);
    a.setTrustlyOrderId(TRUSTLY_ORDER_ID);
    a.setStatus(status);
    return Lists.newArrayList(a);
  }
}
