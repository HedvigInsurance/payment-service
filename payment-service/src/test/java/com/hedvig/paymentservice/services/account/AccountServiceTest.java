package com.hedvig.paymentservice.services.account;

import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus;
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
  public void Test() {
    Mockito.when(memberRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

    assertThat(accountService.getdirectDebitStatus(Mockito.anyString())).isEqualTo(DirectDebitStatus.NEEDS_SETUP);
  }
}
