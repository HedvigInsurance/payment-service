package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import com.hedvig.paymentservice.web.dtos.PaymentMemberDTO;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/_/members/")
public class MemberController {

  private final PaymentService paymentService;
  private final MemberRepository memberRepository;

  public MemberController(PaymentService paymentService, MemberRepository memberRepository, ProductPricingService productPricingService) {
    this.paymentService = paymentService;
    this.memberRepository = memberRepository;
  }

  @PostMapping(path = "{memberId}/charge")
  public ResponseEntity<?> chargeMember(
    @PathVariable String memberId, @RequestBody ChargeRequest request) {
    val chargeMemberRequest = new ChargeMemberRequest(memberId, request.getAmount(), request.getRequestedBy());
    val result = paymentService.chargeMember(chargeMemberRequest);

    if (result.getType() != ChargeMemberResultType.SUCCESS) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
    }

    return ResponseEntity.accepted().body("");
  }

  @Deprecated
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

    log.info("Member was created with memberId {}", memberId);
    return ResponseEntity.ok().body(res);
  }

  @GetMapping(path = "{memberId}/transactions")
  public ResponseEntity<PaymentMemberDTO> getTransactionsByMember(@PathVariable String memberId) {

    val memberMaybe = memberRepository.findById(memberId);

    if (memberMaybe.isPresent()) {
      val member = memberMaybe.get();
      return ResponseEntity.ok(PaymentMemberDTO.Companion.fromMember(member));
    }

    return ResponseEntity.notFound().build();
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
        .map(m -> new DirectDebitStatusDTO(m.getId(), m.isDirectDebitMandateActive()))
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
