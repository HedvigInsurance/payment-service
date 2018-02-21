package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.SelectAccountDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("_/member/{memberId}/trustly")
public class TrustlyController {

    final TrustlyService service;

    public TrustlyController(TrustlyService service) {
        this.service = service;
    }

    @GetMapping("/registerDirectDebit")
    public ResponseEntity<SelectAccountDTO> getRegisterDirectDebit(){

        return ResponseEntity.ok(new SelectAccountDTO("", "", "", "", "", ""));
    }

    @PostMapping("/registerDirectDebit")
    public ResponseEntity<DirectDebitResponse> postRegisterDirectDebit(@PathVariable String memberId, @RequestBody DirectDebitRequest requestData)  {
        final DirectDebitResponse directDebitResponse = service.requestDirectDebitAccount(memberId, requestData);

        return ResponseEntity.ok(directDebitResponse);
    }

}
