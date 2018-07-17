package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

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
            request.getAmount()
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

        val res = new HashMap<String, String>();
        res.put("memberId", memberId);
        return ResponseEntity.ok().body(res);
    }

    @GetMapping(path = "{memberId}/transactions")
    public ResponseEntity<Member> getTransactionsByMember(@PathVariable String memberId) {
        val member = memberRepository
            .findById(memberId)
            .orElseThrow(() -> new RuntimeException("Could not find member"));

        return ResponseEntity.ok().body(member);
    }


    @PostMapping(path = "{memberId}/updateTrustlyAccount")
    public ResponseEntity<?> updateTrustlyAccount(@RequestBody UpdateTrustlyAccountCommand cmd) {

        paymentService.sendCommand(cmd);

        return ResponseEntity.ok(cmd.getMemberId());
    }

    //Returns a boolean with the direct debit status for a specific member.
    @GetMapping(path ="{memberId}/checkDirectDebitStatus")
    public ResponseEntity<Boolean> checkDirectDebitByMemberId(@PathVariable String memberId){
        Optional<Boolean> isConnected = memberRepository.findByIdAndDirectDebitMandateActiveTrue(memberId);

        if (isConnected.isPresent()){
            return ResponseEntity.ok(isConnected.get());
        }
        return ResponseEntity.notFound().build();
    }

    //Returns an array of memberIds with the corresponding status of direct debit.
    @GetMapping(path = "/directDebitList")
    public ResponseEntity<?> checkDirectDebitByStatus(@RequestParam(name = "active") Boolean isStatusActive){

        List<Member> listOfMembers = memberRepository.findByDirectDebitMandateActive(isStatusActive);

        return ResponseEntity.ok(listOfMembers.stream().map(Member::getId).toArray());
    }
}
