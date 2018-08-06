package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
  public ResponseEntity<?> chargeMember(
      @PathVariable String memberId, @RequestBody ChargeRequest request) {

    val chargeMemberRequest = new ChargeMemberRequest(memberId, request.getAmount());
    val res = paymentService.chargeMember(chargeMemberRequest);

    if (res == false) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
    }

    return ResponseEntity.accepted().body("");
  }

  @PostMapping(path = "{memberId}/payout")
  public ResponseEntity<?> payoutMember(
      @PathVariable String memberId, @RequestBody PayoutRequest request) {
    val payoutMemberRequest =
        new PayoutMemberRequest(
            memberId,
            request.getAmount(),
            request.getAddress(),
            request.getCountryCode(),
            request.getDateOfBirth(),
            request.getFirstName(),
            request.getLastName());

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

    val member =
        memberRepository
            .findById(memberId)
            .orElse(new Member()); // Return an empty Member Object if the member does not exist
    // The empty object will not break back-office

    return ResponseEntity.ok().body(member);
  }

  @PostMapping(path = "{memberId}/updateTrustlyAccount")
  public ResponseEntity<?> updateTrustlyAccount(@RequestBody UpdateTrustlyAccountCommand cmd) {

    paymentService.sendCommand(cmd);

    return ResponseEntity.ok(cmd.getMemberId());
  }

  @GetMapping(path = "/directDebitStatus/[{memberIds}]")
  public ResponseEntity<List<DirectDebitStatusDTO>> getDirectDebitStatuses(
      @PathVariable("memberIds") List<String> memberIds) {

    val members =
        memberRepository
            .findAllByIdIn(memberIds)
            .stream()
            .map(m -> new DirectDebitStatusDTO(m.getId(), m.getDirectDebitMandateActive()))
            .collect(Collectors.toList());

    if (memberIds.size() != members.size()) {
      log.info(
          "List size mismatch: memberIds.size = {}, members.size = {} The rest of the member ids with be replaced with false!",
          memberIds.size(),
          members.size());

      val membersWithPaymentStatus =
          members.stream().map(DirectDebitStatusDTO::getMemberId).collect(Collectors.toList());

      memberIds
          .stream()
          .filter(x -> !membersWithPaymentStatus.contains(x))
          .collect(Collectors.toList());

      for (String id : memberIds) {
        members.add(new DirectDebitStatusDTO(id, false));
      }
    }

    return ResponseEntity.ok(members);
  }
}
