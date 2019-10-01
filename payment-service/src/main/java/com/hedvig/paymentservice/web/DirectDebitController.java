package com.hedvig.paymentservice.web;

import com.hedvig.paymentservice.domain.payments.DirectDebitStatus;
import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitOrderInfo;
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

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/directDebit/")
public class DirectDebitController {

  private Logger logger = LoggerFactory.getLogger(DirectDebitController.class);

  private MemberRepository memberRepository;
  private TrustlyService trustlyService;

  public DirectDebitController(MemberRepository memberRepository, TrustlyService trustlyService) {
    this.memberRepository = memberRepository;
    this.trustlyService = trustlyService;
  }

  @GetMapping(path = "status")
  public ResponseEntity<DirectDebitStatusDTO> getDirectDebitStatus(
    @RequestHeader(name = "hedvig.token") String memberId
  ) {

    logger.debug("Fetching status for member {}", memberId);

    Optional<Member> om = memberRepository.findById(memberId);

    if (!om.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    Member member = om.get();

    return ResponseEntity.ok(new DirectDebitStatusDTO(member.getId(), member.getDirectDebitStatus().equals(DirectDebitStatus.CONNECTED)));
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
        req.getClientContext()
      );

    return ResponseEntity.ok(response);
  }

}
