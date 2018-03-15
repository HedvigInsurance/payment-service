package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.val;

@RestController
@RequestMapping(path = "/_/members/")
public class MemberController {

    private final PaymentService paymentService;
    private final MemberRepository memberRepository;

    public MemberController(PaymentService paymentService, MemberRepository memberRepository) {
        this.paymentService = paymentService;
        this.memberRepository = memberRepository;
    }

    @PostMapping(path = "{memberId}/charge")
    public ResponseEntity<?> chargeMember(@PathVariable String memberId, @RequestBody ChargeRequest request) {

        val chargeMemberRequest = new ChargeMemberRequest(
            memberId,
            request.getAmount(),
            request.getEmail()
        );
        val res = paymentService.chargeMember(chargeMemberRequest);

        if (res == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }

        return ResponseEntity.accepted().body("");
    }

    @PostMapping(path = "{memberId}/payout")
    public ResponseEntity<?> payoutMember(@PathVariable String memberId, @RequestBody PayoutRequest request) {
        val payoutMemberRequest = new PayoutMemberRequest(
            memberId,
            request.getAmount(),
            request.getAddress(),
            request.getCountryCode(),
            request.getDateOfBirth(),
            request.getFirstName(),
            request.getLastName()
        );

        val res = paymentService.payoutMember(payoutMemberRequest);
        if (res == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
        return ResponseEntity.accepted().body("");
    }

    @PostMapping(path = "{memberId}/create")
    public ResponseEntity<?> createMember(@PathVariable String memberId) {
        paymentService.createMember(memberId);

        return ResponseEntity.ok().body(new HashMap<String, String>() {{put("memberId", memberId);}});
    }

    @GetMapping(path = "{memberId}/transactions")
    public ResponseEntity<Member> getTransactionsByMember(@PathVariable String memberId) {
        val member = memberRepository
            .findById(memberId)
            .orElseThrow(() -> new RuntimeException("Could not find member"));

        return ResponseEntity.ok().body(member);
    }
}
