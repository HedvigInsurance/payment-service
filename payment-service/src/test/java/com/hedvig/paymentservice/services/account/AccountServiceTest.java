package com.hedvig.paymentservice.services.account;

import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

  private static String MEMBER_ID = "12345";

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private AccountRegistrationRepository accountRegistrationRepository;
  @Mock
  private ProductPricingService productPricingService;

  private AccountService accountService;

  @Before
  public void setUp() {
    accountService = new AccountServiceImpl(memberRepository, accountRegistrationRepository, productPricingService);
  }

  @Test
  public void When_memberDoesNotExistInPaymentService_Then_Return_NeedSetup() {
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

    assertThat(accountService.getdirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }

  @Test
  public void When_memberExistInPaymentServiceWuthoutDirectDebitStatus_Then_Return_NeedSetup(){
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.of(makeMember(MEMBER_ID)));

    assertThat(accountService.getdirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }

  private Member makeMember(String memberId){
    Member member = new Member();
    member.setId(memberId);
    return member;
  }
}
