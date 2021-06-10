package com.hedvig.paymentservice.web;

import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/directDebit/")
public class DirectDebitController {

    private Logger logger = LoggerFactory.getLogger(DirectDebitController.class);

    private TrustlyService trustlyService;
    private BankAccountService bankAccountService;

    public DirectDebitController(
        TrustlyService trustlyService,
        BankAccountService bankAccountService
    ) {
        this.trustlyService = trustlyService;
        this.bankAccountService = bankAccountService;
    }


    @GetMapping(path = "status")
    public ResponseEntity<DirectDebitStatusDTO> getDirectDebitStatus(
        @RequestHeader(name = "hedvig.token") String memberId
    ) {
        final PayinMethodStatus status = bankAccountService.getPayinMethodStatus(memberId);

        return ResponseEntity.ok(new DirectDebitStatusDTO(
            memberId,
            status == PayinMethodStatus.ACTIVE,
            status)
        );
    }

    @PostMapping(path = "register")
    public ResponseEntity<DirectDebitResponse> registerDirectDebit(
        @RequestHeader(name = "hedvig.token") String memberId,
        @RequestBody @Valid RegisterDirectDebitRequestDTO req
    ) {

        logger.info("Starting register directDebit for member {}", memberId);

        final DirectDebitResponse response = trustlyService.requestDirectDebitAccount(
            new DirectDebitOrderInfo(memberId, req, false),
            req.getClientContext() == null ? null : req.getClientContext().getSuccessUrl(),
            req.getClientContext() == null ? null : req.getClientContext().getFailureUrl()
        );

        return ResponseEntity.ok(response);
    }

}
