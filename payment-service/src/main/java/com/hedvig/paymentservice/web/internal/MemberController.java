package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
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

    public MemberController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
            return ResponseEntity.status(403).body("");
        }

        return ResponseEntity.accepted().body("");
    }

    @PostMapping(path = "{memberId}/create")
    public ResponseEntity<?> createMember(@PathVariable String memberId) {
        paymentService.createMember(memberId);

        return ResponseEntity.ok().body(new HashMap<String, String>() {{put("memberId", memberId);}});
    }
}
