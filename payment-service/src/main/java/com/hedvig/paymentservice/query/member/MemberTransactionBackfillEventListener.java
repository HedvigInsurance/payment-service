package com.hedvig.paymentservice.query.member;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.PayoutCreatedEvent;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import lombok.val;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ProcessingGroup("MemberTransactionBackfillEventListener")
public class MemberTransactionBackfillEventListener {
    private static final Logger log = LoggerFactory.getLogger(MemberTransactionBackfillEventListener.class);
    private final MemberRepository memberRepository;
    private final TransactionRepository txRepository;

    public MemberTransactionBackfillEventListener(final MemberRepository memberRepository, final TransactionRepository txRepository) {
        this.memberRepository = memberRepository;
        this.txRepository = txRepository;
    }

    @EventHandler
    public void on(ChargeCreatedEvent e) {
        saveMemberOnTransaction(e.getMemberId(), e.getTransactionId());
        log.info("Backfilled transaction {} for member {} for charge created event at {}", e.getTransactionId(), e.getMemberId(), e.getTimestamp());
    }

    @EventHandler
    public void on(PayoutCreatedEvent e) {
        if (e.getCategory() == TransactionCategory.CLAIM) {
            saveMemberOnTransaction(e.getMemberId(), e.getTransactionId());
            log.info("Backfilled transaction {} for member {} for payout created event at {}", e.getTransactionId(), e.getMemberId(), e.getTimestamp());
        }
    }

    private void saveMemberOnTransaction(final String memberId, final UUID transactionId) {
        final Member member =
            memberRepository
                .findById(memberId)
                .orElseThrow(() -> new RuntimeException("Could not find member with id " + memberId));
        final Transaction transaction = txRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Could not find transaction with id " + transactionId.toString()));

        transaction.setMember(member);

        txRepository.save(transaction);
    }
}
