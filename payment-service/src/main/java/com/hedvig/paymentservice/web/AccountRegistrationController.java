package com.hedvig.paymentservice.web;


import com.hedvig.paymentservice.domain.trustlyOrder.commands.CancelNotificationReceivedCommand;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/accountRegistration")
public class AccountRegistrationController {

  private CommandGateway commandGateway;
  private AccountRegistrationRepository repository;

  @Autowired
  public AccountRegistrationController(CommandGateway commandGateway, AccountRegistrationRepository repository) {
    this.commandGateway = commandGateway;
    this.repository = repository;
  }


  @PostMapping("/cancelRequested")
    public ResponseEntity<?> cancelRequestedAccountRegistration(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    List<AccountRegistration> list = repository.findRequestedRegistrationByDate(date.atStartOfDay().atZone(ZoneId.of("Europe/Stockholm")).toInstant());

    log.info("Found {} requested registrations", list.size());

    list.forEach(r ->
      commandGateway.sendAndWait(
        new CancelNotificationReceivedCommand(
          r.getHedvigOrderId(),
          UUID.randomUUID().toString(),
          r.getTrustlyOrderId(),
          r.getMemberId())));

    log.info("Cancel triggered!");

    return ResponseEntity.ok().build();
  }
}
