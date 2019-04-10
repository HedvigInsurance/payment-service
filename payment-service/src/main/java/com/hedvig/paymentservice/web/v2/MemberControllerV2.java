package com.hedvig.paymentservice.web.v2;

import com.hedvig.paymentservice.domain.payments.TransactionCategory;
import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat;
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequestDTO;
import com.hedvig.paymentservice.web.dtos.PayoutRequestDTO;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Nullable;

@Slf4j
@RestController
@RequestMapping(path = "/v2/_/members/")
public class MemberControllerV2 {

  private final PaymentService paymentService;
  private final MemberService memberService;
  private final Meerkat meerkat;

  @Autowired
  public MemberControllerV2(
      PaymentService paymentService,
      MemberService memberService,
      Meerkat meerkat
  ) {
    this.paymentService = paymentService;
    this.memberService = memberService;
    this.meerkat = meerkat;
  }

  @PostMapping(path = "{memberId}/payout")
  public ResponseEntity<UUID> payoutMember(
      @PathVariable String memberId,
      @RequestParam(name="category", required = false, defaultValue = "CLAIM") TransactionCategory category,
      @Nullable @RequestParam(name="referenceId", required = false) String referenceId,
      @Nullable @RequestParam(name="note", required = false) String note,
      @RequestBody PayoutRequestDTO request
  ) {

    Optional<Member> optionalMember = memberService.getMember(memberId);
    if (!optionalMember.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    val member = optionalMember.get();

    SanctionStatus memberStatus = meerkat
        .getMemberSanctionStatus(member.getFirstName() + ' ' + member.getLastName());

    if (memberStatus.equals(SanctionStatus.FullHit)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    if (!request.isSanctionBypassed()
        && (memberStatus.equals(SanctionStatus.Undetermined)
        || memberStatus.equals(SanctionStatus.PartialHit))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    PayoutMemberRequestDTO payoutMemberRequest = new PayoutMemberRequestDTO(
      request.getAmount(),
      category,
      referenceId,
      note
    );

    Optional<UUID> result = paymentService.payoutMember(memberId, member, payoutMemberRequest);

    return result.map(uuid -> ResponseEntity.accepted().body(uuid))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }
}
