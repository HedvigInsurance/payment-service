package com.hedvig.paymentservice.web;


import com.hedvig.paymentservice.domain.accountRegistration.commands.ReceiveAccountRegistrationCancellationCommand;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistration;
import com.hedvig.paymentservice.query.registerAccount.enteties.AccountRegistrationRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping(path = "/accountRegistration")
public class AccountRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(AccountRegistrationController.class);
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
                new ReceiveAccountRegistrationCancellationCommand(r.getAccountRegistrationId(),
                    r.getHedvigOrderId(),
                    r.getMemberId())));

        log.info("Cancel triggered!");

        return ResponseEntity.ok().build();
    }
}
