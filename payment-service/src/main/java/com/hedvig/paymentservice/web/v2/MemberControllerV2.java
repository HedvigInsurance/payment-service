package com.hedvig.paymentservice.web.v2;

import com.hedvig.paymentservice.serviceIntergration.meerkat.Meerkat;
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import com.hedvig.paymentservice.services.payments.PaymentService;
import java.util.Optional;
import java.util.UUID;
import javax.money.MonetaryAmount;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/v2/_/members/")
public class MemberControllerV2 {

  private final PaymentService paymentService;
  private final MemberService memberService;
  private final Meerkat meerkat;

  public MemberControllerV2(PaymentService paymentService,
      MemberService memberService,
      Meerkat meerkat) {
    this.paymentService = paymentService;
    this.memberService = memberService;
    this.meerkat = meerkat;
  }

  @PostMapping(path = "{memberId}/payout")
  public ResponseEntity<UUID> payoutMember(
      @PathVariable String memberId, @RequestBody MonetaryAmount amount) {

    Optional<Member> optionalMember = memberService.getMember(memberId);
    if (!optionalMember.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    val member = optionalMember.get();

    SanctionStatus memberStatus = meerkat
        .getMemberSanctionStatus(member.getFirstName() + " " + member.getLastName());
    if (memberStatus.equals(SanctionStatus.FullHit)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Optional<UUID> result = paymentService.payoutMember(memberId, member, amount);

    return result.map(uuid -> ResponseEntity.accepted().body(uuid))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
  }
}
