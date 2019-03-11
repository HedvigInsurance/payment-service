package com.hedvig.paymentservice.services.account;

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus;
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.InsuranceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

  private MemberRepository memberRepository;
  private AccountRegistrationRepository accountRegistrationRepository;
  private ProductPricingService productPricingService;

  @Autowired
  public AccountServiceImpl(MemberRepository memberRepository, AccountRegistrationRepository accountRegistrationRepository, ProductPricingService productPricingService) {
    this.memberRepository = memberRepository;
    this.accountRegistrationRepository = accountRegistrationRepository;
    this.productPricingService = productPricingService;
  }

  public BankAccount getBankAccount(String memberId) {

    if (memberId == null) {
      log.error("GetBankAccountInfo - hedvig.token is missing");
      return null;
    }
    Optional<Member> optionalMember = memberRepository.findById(memberId);

    return optionalMember.filter(x -> x.getDirectDebitStatus() == DirectDebitStatus.CONNECTED).map(BankAccount::fromMember).orElse(null);
  }

  //TODO: Catch Red days - Weekends
  public LocalDate getNextChargeDate(String memberId) {
    if (memberId == null) {
      log.error("registerAccountProcessingStatus - hedvig.token is missing");
      return null;
    }

    Optional<InsuranceStatus> status = productPricingService.getInsuranceStatus(memberId);

    if (!status.isPresent() || status.get() != InsuranceStatus.ACTIVE) {
      return null;
    }

    return LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonth(), 27);
  }

  public com.hedvig.paymentservice.graphQl.types.DirectDebitStatus getDirectDebitStatus(String memberId) {
    if (memberId == null) {
      log.error("registerAccountProcessingStatus - hedvig.token is missing");
      return null;
    }

    AccountRegistration accountRegistration = accountRegistrationRepository.
      findByMemberId(memberId).stream()
      .max(Comparator.comparing(AccountRegistration::getInitiated))
      .orElse(null);

    Optional<Member> optionalMember = memberRepository.findById(memberId);

    if (optionalMember.isPresent()) {
      Member member = optionalMember.get();
      if (member.getDirectDebitStatus() != null && member.getDirectDebitStatus().equals(DirectDebitStatus.CONNECTED)) {
        if (accountRegistration == null || accountRegistration.getStatus().equals(AccountRegistrationStatus.CONFIRMED) || accountRegistration.getStatus().equals(AccountRegistrationStatus.CANCELLED)) {
          return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.ACTIVE;
        } else {
          return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.PENDING;
        }
      } else if (member.getDirectDebitStatus() != null && member.getDirectDebitStatus().equals(DirectDebitStatus.DISCONNECTED)) {
        if (accountRegistration == null || accountRegistration.getStatus().equals(AccountRegistrationStatus.CONFIRMED) || accountRegistration.getStatus().equals(AccountRegistrationStatus.CANCELLED)) {
          return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.NEEDS_SETUP;
        } else {
          return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.PENDING;
        }
      }
    }
    return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.NEEDS_SETUP;
  }

}
