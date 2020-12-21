package com.hedvig.paymentservice.query.member;

import com.hedvig.paymentservice.domain.payments.TransactionStatus;
import com.hedvig.paymentservice.domain.payments.TransactionType;
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.AdyenAccountUpdatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent;
import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutFailedEvent;
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(0)
public class MemberEventListener {

    private static final Logger log = LoggerFactory.getLogger(MemberEventListener.class);
    private final MemberRepository memberRepository;

    public MemberEventListener(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @EventHandler
    public void on(MemberCreatedEvent e) {
        Member member = new Member();
        member.setId(e.getMemberId());
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCreatedEvent e) {
        Member member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        Transaction transaction = new Transaction();
        transaction.setId(e.getTransactionId());
        transaction.setAmount(e.getAmount().getNumber().numberValueExact(BigDecimal.class));
        transaction.setCurrency(e.getAmount().getCurrency().getCurrencyCode());
        transaction.setTimestamp(e.getTimestamp());
        transaction.setTransactionType(TransactionType.CHARGE);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);
        transaction.setMember(member);

        Map<UUID, Transaction> transactions = member.getTransactions();
        transactions.put(transaction.getId(), transaction);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeFailedEvent e) {
        Member member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        Map<UUID, Transaction> transactions = member.getTransactions();
        Transaction transaction = transactions.get(e.getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutCreatedEvent e) {
        Member member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        Transaction transaction = new Transaction();
        transaction.setId(e.getTransactionId());
        transaction.setAmount(e.getAmount().getNumber().numberValueExact(BigDecimal.class));
        transaction.setCurrency(e.getAmount().getCurrency().getCurrencyCode());
        transaction.setTimestamp(e.getTimestamp());
        transaction.setTransactionType(TransactionType.PAYOUT);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);
        transaction.setMember(member);

        Map<UUID, Transaction> transactions = member.getTransactions();
        transactions.put(e.getTransactionId(), transaction);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCompletedEvent e) {
        Optional<Member> maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }
        Member member = maybeMember.get();

        Transaction transaction = member.getTransactions().get(e.getTransactionId());

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutCompletedEvent e) {
        Optional<Member> maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }

        final Member member = maybeMember.get();
        Transaction transaction = member.getTransaction(e.getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutFailedEvent e) {
        Optional<Member> maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }
        Member member = maybeMember.get();

        Transaction transaction = member.getTransaction(e.getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        memberRepository.save(member);
    }


    @EventHandler
    public void on(AdyenAccountCreatedEvent e) {
        Optional<Member> member = memberRepository.findById(e.getMemberId());

        if (!member.isPresent()) {
            log.error("Could not find member");
            return;
        }

        Member m = member.get();

        m.setAdyenRecurringDetailReference(e.getRecurringDetailReference());
        m.setPayinMethodStatus(PayinMethodStatus.Companion.fromAdyenAccountStatus(e.getAccountStatus()));

        memberRepository.save(m);
    }

    @EventHandler
    public void on(AdyenAccountUpdatedEvent e) {
        Optional<Member> member = memberRepository.findById(e.getMemberId());

        if (!member.isPresent()) {
            log.error("Could not find member");
            return;
        }

        Member m = member.get();

        m.setAdyenRecurringDetailReference(e.getRecurringDetailReference());
        m.setPayinMethodStatus(PayinMethodStatus.Companion.fromAdyenAccountStatus(e.getAccountStatus()));

        memberRepository.save(m);
    }

    @ResetHandler
    public void onReset() {
        memberRepository.deleteAll();
    }
}
