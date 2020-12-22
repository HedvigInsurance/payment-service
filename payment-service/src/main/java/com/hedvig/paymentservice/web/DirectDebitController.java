package com.hedvig.paymentservice.web;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.serviceIntergration.productPricing.ProductPricingService;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market;
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo;
import com.hedvig.paymentservice.web.dtos.DirectDebitAccountOrderDTO;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import com.hedvig.paymentservice.web.dtos.RegisterDirectDebitRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.error.Mark;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/directDebit/")
public class DirectDebitController {

    private Logger logger = LoggerFactory.getLogger(DirectDebitController.class);

    private MemberRepository memberRepository;
    private TrustlyService trustlyService;
    private BankAccountService bankAccountService;

    public DirectDebitController(
        MemberRepository memberRepository,
        TrustlyService trustlyService,
        BankAccountService bankAccountService
    ) {
        this.memberRepository = memberRepository;
        this.trustlyService = trustlyService;
        this.bankAccountService = bankAccountService;
    }


    @GetMapping(path = "status")
    public ResponseEntity<DirectDebitStatusDTO> getDirectDebitStatus(
        @RequestHeader(name = "hedvig.token") String memberId
    ) {
        logger.debug("Fetching status for member {}", memberId);

        Optional<Member> optionalMember = memberRepository.findById(memberId);

        if (!optionalMember.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Member member = optionalMember.get();

       if (member.getAdyenRecurringDetailReference() != null) {
            return ResponseEntity.ok(
                new DirectDebitStatusDTO(
                    memberId,
                    member.getPayinMethodStatus() == PayinMethodStatus.ACTIVE
                )
            );
        }

        final DirectDebitAccountOrderDTO latestDirectDebitAccountOrder = bankAccountService.getLatestDirectDebitAccountOrder(memberId);

        if (latestDirectDebitAccountOrder != null) {
            return ResponseEntity.ok(new DirectDebitStatusDTO(memberId,
                latestDirectDebitAccountOrder.getDirectDebitStatus() == DirectDebitStatus.CONNECTED));
        }

        return ResponseEntity.ok(new DirectDebitStatusDTO(memberId, false));
    }

    @PostMapping(path = "register")
    public ResponseEntity<DirectDebitResponse> registerDirectDebit(
        @RequestHeader(name = "hedvig.token") String memberId,
        @RequestBody @Valid RegisterDirectDebitRequestDTO req
    ) {

        logger.info("Starting register directDebit for member {}", memberId);

        final DirectDebitResponse response = trustlyService
            .requestDirectDebitAccount(
                new DirectDebitOrderInfo(memberId, req, false),
                req.getClientContext() == null ? null : req.getClientContext().getSuccessUrl(),
                req.getClientContext() == null ? null : req.getClientContext().getFailureUrl()
            );

        return ResponseEntity.ok(response);
    }

}
