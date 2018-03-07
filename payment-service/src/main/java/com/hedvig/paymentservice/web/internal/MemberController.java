package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
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
            request.getCurrency(),
            request.getEmail()
        );
        // TODO: Validate the response we get from here?
        paymentService.chargeMember(chargeMemberRequest);

        return ResponseEntity.accepted().body("");
    }
}
