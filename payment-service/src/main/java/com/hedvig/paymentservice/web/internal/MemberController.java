package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus;
import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.domain.payments.commands.UpdateTrustlyAccountCommand;
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistration;
import com.hedvig.paymentservice.query.adyenTokenRegistration.entities.AdyenTokenRegistrationRepository;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService;
import com.hedvig.paymentservice.services.payments.PaymentService;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResult;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberResultType;
import com.hedvig.paymentservice.services.payments.dto.PayoutMemberRequest;
import com.hedvig.paymentservice.web.dtos.ChargeRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitAccountOrderDTO;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import com.hedvig.paymentservice.web.dtos.PaymentMemberDTO;
import com.hedvig.paymentservice.web.dtos.PayoutMethodStatusDTO;
import com.hedvig.paymentservice.web.dtos.PayoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

@RestController
@RequestMapping(path = "/_/members/")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final PaymentService paymentService;
    private final MemberRepository memberRepository;
    private final BankAccountService bankAccountService;
    private final AdyenTokenRegistrationRepository adyenTokenRegistrationRepository;

    public MemberController(
        PaymentService paymentService,
        MemberRepository memberRepository,
        BankAccountService bankAccountService,
        AdyenTokenRegistrationRepository adyenTokenRegistrationRepository
    ) {
        this.paymentService = paymentService;
        this.memberRepository = memberRepository;
        this.bankAccountService = bankAccountService;
        this.adyenTokenRegistrationRepository = adyenTokenRegistrationRepository;
    }

    @PostMapping(path = "{memberId}/charge")
    public ResponseEntity<?> chargeMember(
        @PathVariable String memberId, @RequestBody ChargeRequest request) {
        ChargeMemberRequest chargeMemberRequest = new ChargeMemberRequest(memberId, request.getAmount(), request.getRequestedBy());
        ChargeMemberResult result = paymentService.chargeMember(chargeMemberRequest);

        if (result.getType() != ChargeMemberResultType.SUCCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }

        return ResponseEntity.accepted().body("");
    }

    @Deprecated
    @PostMapping(path = "{memberId}/payout")
    public ResponseEntity<?> payoutMember(
        @PathVariable String memberId, @RequestBody PayoutRequest request) {
        PayoutMemberRequest payoutMemberRequest =
            new PayoutMemberRequest(
                memberId,
                request.getAmount(),
                request.getAddress(),
                request.getCountryCode(),
                request.getDateOfBirth(),
                request.getFirstName(),
                request.getLastName());

        boolean res = paymentService.payoutMember(payoutMemberRequest);
        if (res == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
        return ResponseEntity.accepted().body("");
    }

    @PostMapping(path = "{memberId}/create")
    public ResponseEntity<?> createMember(@PathVariable String memberId) {
        paymentService.createMember(memberId);

        HashMap<String, String> res = new HashMap<String, String>();
        res.put("memberId", memberId);

        log.info("Member was created with memberId {}", memberId);
        return ResponseEntity.ok().body(res);
    }

    @GetMapping(path = "{memberId}/transactions")
    public ResponseEntity<PaymentMemberDTO> getTransactionsByMember(@PathVariable String memberId) {

        Optional<Member> memberMaybe = memberRepository.findById(memberId);

        if (memberMaybe.isPresent()) {
            Member member = memberMaybe.get();

            final DirectDebitAccountOrderDTO latestDirectDebitAccountOrder = bankAccountService.getLatestDirectDebitAccountOrder(memberId);

            return ResponseEntity.ok(PaymentMemberDTO.Companion.fromMember(member, latestDirectDebitAccountOrder));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "{memberId}/updateTrustlyAccount")
    public ResponseEntity<?> updateTrustlyAccount(@RequestBody UpdateTrustlyAccountCommand cmd) {

        paymentService.sendCommand(cmd);

        return ResponseEntity.ok(cmd.getMemberId());
    }

    @GetMapping(path = "/directDebitStatus/[{memberIds}]")
    public ResponseEntity<List<DirectDebitStatusDTO>> getDirectDebitStatuses(@PathVariable("memberIds") List<String> memberIds) {
        throw new RuntimeException("Deprecated: Attempted to call function /directDebitStatus/[{memberIds}]" +
            " on getDirectDebitStatuses");
    }

    @GetMapping("/{memberId}/payoutMethod/status")
    public ResponseEntity<PayoutMethodStatusDTO> getPayoutMethodStatus(@PathVariable String memberId) {
        List<AdyenTokenRegistration> registrations = adyenTokenRegistrationRepository
            .findByMemberIdAndTokenStatusAndIsForPayoutIsTrue(memberId, AdyenTokenRegistrationStatus.AUTHORISED);

        if (!registrations.isEmpty()) {
            return ResponseEntity.ok(new PayoutMethodStatusDTO(memberId, true));
        }

        DirectDebitAccountOrderDTO latestOrder = bankAccountService.getLatestDirectDebitAccountOrder(memberId);
        if (latestOrder != null) {
            return ResponseEntity.ok(
                new PayoutMethodStatusDTO(
                    memberId,
                    latestOrder.getDirectDebitStatus() == DirectDebitStatus.CONNECTED
                )
            );
        }

        return ResponseEntity.ok(new PayoutMethodStatusDTO(memberId, false));
    }
}
