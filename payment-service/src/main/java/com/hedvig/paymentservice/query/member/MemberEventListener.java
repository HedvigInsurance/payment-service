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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
@Order(0)
@ProcessingGroup("MemberEventsProcessorGroup")
public class MemberEventListener {

    private final MemberRepository memberRepository;

    public MemberEventListener(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @EventHandler
    public void on(MemberCreatedEvent e) {
        val member = new Member();
        member.setId(e.getMemberId());
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCreatedEvent e) {
        val member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        val transaction = new Transaction();
        transaction.setId(e.getTransactionId());
        transaction.setAmount(e.getAmount().getNumber().numberValueExact(BigDecimal.class));
        transaction.setCurrency(e.getAmount().getCurrency().getCurrencyCode());
        transaction.setTimestamp(e.getTimestamp());
        transaction.setTransactionType(TransactionType.CHARGE);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);
        transaction.setMember(member);

        val transactions = member.getTransactions();
        transactions.put(transaction.getId(), transaction);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeFailedEvent e) {
        val member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        val transactions = member.getTransactions();
        val transaction = transactions.get(e.getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutCreatedEvent e) {
        val member =
            memberRepository
                .findById(e.getMemberId())
                .orElseThrow(() -> new RuntimeException("Could not find member"));
        val transaction = new Transaction();
        transaction.setId(e.getTransactionId());
        transaction.setAmount(e.getAmount().getNumber().numberValueExact(BigDecimal.class));
        transaction.setCurrency(e.getAmount().getCurrency().getCurrencyCode());
        transaction.setTimestamp(e.getTimestamp());
        transaction.setTransactionType(TransactionType.PAYOUT);
        transaction.setTransactionStatus(TransactionStatus.INITIATED);
        transaction.setMember(member);

        val transactions = member.getTransactions();
        transactions.put(e.getTransactionId(), transaction);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(ChargeCompletedEvent e) {
        val maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }
        val member = maybeMember.get();

        val transaction = member.getTransactions().get(e.getTransactionId());

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutCompletedEvent e) {
        val maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }

        final Member member = maybeMember.get();
        val transaction = member.getTransaction(e.getTransactionId());
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        memberRepository.save(member);
    }

    @EventHandler
    public void on(PayoutFailedEvent e) {
        val maybeMember = memberRepository.findById(e.getMemberId());
        if (maybeMember.isPresent() == false) {
            log.error("Could not find member");
            return;
        }
        val member = maybeMember.get();

        val transaction = member.getTransaction(e.getTransactionId());
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
