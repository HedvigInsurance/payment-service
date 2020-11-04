package com.hedvig.paymentservice.services.bankAccounts;

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus;
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.BankAccount;
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private MemberRepository memberRepository;
    private AccountRegistrationRepository accountRegistrationRepository;
    private ProductPricingService productPricingService;

    @Autowired
    public BankAccountServiceImpl(MemberRepository memberRepository, AccountRegistrationRepository accountRegistrationRepository, ProductPricingService productPricingService) {
        this.memberRepository = memberRepository;
        this.accountRegistrationRepository = accountRegistrationRepository;
        this.productPricingService = productPricingService;
    }

    public BankAccount getBankAccount(String memberId) {

        if (memberId == null) {
            log.error("GetBankAccountInfo - hedvig.token is missing");
            throw new NullPointerException("GetBankAccountInfo - hedvig.token is missing");
        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        return optionalMember.filter(x -> x.getDirectDebitStatus() == DirectDebitStatus.CONNECTED).map(BankAccount::fromMember).orElse(null);
    }

    public LocalDate getNextChargeDate(String memberId) {
        if (memberId == null) {
            log.error("GetNextChargeDate - hedvig.token is missing");
            throw new NullPointerException("GetNextChargeDate - hedvig.token is missing");
        }

        boolean hasContractActiveCurrentMonth = productPricingService.hasContractActiveCurrentMonth(memberId);
        if (!hasContractActiveCurrentMonth) {
            return null;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Stockholm"));
        YearMonth currentPeriod = YearMonth.of(today.getYear(), today.getMonth());
        LocalDate chargeDateCurrentPeriod = getChargeDateOfPeriod(currentPeriod);
        if (!today.isAfter(chargeDateCurrentPeriod)) {
            return chargeDateCurrentPeriod;
        }
        return getChargeDateOfPeriod(currentPeriod.plusMonths(1));
    }

    //TODO: Handle red days
    static LocalDate getChargeDateOfPeriod(YearMonth period) {
        LocalDate chargeDateThisMonth = period.atDay(27);
        if (chargeDateThisMonth.getDayOfWeek() == DayOfWeek.SATURDAY) {
            chargeDateThisMonth = chargeDateThisMonth.plusDays(2);
        }
        if (chargeDateThisMonth.getDayOfWeek() == DayOfWeek.SUNDAY) {
            chargeDateThisMonth = chargeDateThisMonth.plusDays(1);
        }
        return chargeDateThisMonth;
    }

    public com.hedvig.paymentservice.graphQl.types.DirectDebitStatus getDirectDebitStatus(String memberId) {
        if (memberId == null) {
            log.error("GetDirectDebitStatus - hedvig.token is missing");
            throw new NullPointerException("GetDirectDebitStatus - hedvig.token is missing");
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
            } else if (member.getDirectDebitStatus() != null && member.getDirectDebitStatus().equals(DirectDebitStatus.PENDING)) {
                if (accountRegistration == null || accountRegistration.getStatus().equals(AccountRegistrationStatus.CANCELLED)) {
                    return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.NEEDS_SETUP;
                } else {
                    return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.PENDING;
                }
            }
        }
        return com.hedvig.paymentservice.graphQl.types.DirectDebitStatus.NEEDS_SETUP;
    }

    @Override
    public PayinMethodStatus getPayinMethodStatus(String memberId) {
        if (memberId == null) {
            log.error("getPayinMethodStatus - hedvig.token is missing");
            return PayinMethodStatus.NEEDS_SETUP;
        }
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            if (member.getTrustlyAccountNumber() != null) {
                return PayinMethodStatus.Companion.fromTrustlyDirectDebitStatus(getDirectDebitStatus(memberId));
            }

            if (member.getAdyenRecurringDetailReference() != null) {
                return member.getPayinMethodStatus();
            }

        }
        return PayinMethodStatus.NEEDS_SETUP;
    }
}
