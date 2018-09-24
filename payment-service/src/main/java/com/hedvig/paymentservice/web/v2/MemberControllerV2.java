package com.hedvig.paymentservice.web.v2;

import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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

  public MemberControllerV2(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping(path = "{memberId}/payout")
  public ResponseEntity<?> payoutMember(
      @PathVariable String memberId, @RequestBody PayoutRequest request) {

    Optional<UUID> result = paymentService.payoutMember(memberId, request);
    return result.<ResponseEntity<?>>map(uuid -> ResponseEntity.accepted().body(uuid))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }
}
