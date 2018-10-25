package com.hedvig.paymentservice.web;

import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.MemberRepository;
import com.hedvig.paymentservice.web.dtos.DirectDebitStatusDTO;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/directDebit/")
public class DirectDebitController {

  private MemberRepository memberRepository;

  public DirectDebitController(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @GetMapping(path = "status")
  public ResponseEntity<DirectDebitStatusDTO> getDirectDebitStatus(
      @RequestHeader(name = "hedvig.token") String memberId) {

    Optional<Member> om = memberRepository.findById(memberId);

    if (!om.isPresent()) {
      return ResponseEntity.badRequest().build();
    }

    Member member = om.get();

    return ResponseEntity
        .ok(new DirectDebitStatusDTO(member.getId(), member.getDirectDebitMandateActive()));
  }
}
