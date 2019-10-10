package com.hedvig.paymentservice.services.accounts;

import com.hedvig.paymentservice.domain.payments.events.ChargeCompletedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeCreatedEvent;
import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent;
import com.hedvig.paymentservice.serviceIntergration.accountService.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ProcessingGroup("Account")
public class AccountEventListener {

  private AccountService accountService;

  @Autowired
  public AccountEventListener(AccountService accountService) {
    this.accountService = accountService;
  }

  @EventHandler
  public void on(ChargeFailedEvent event) {
    accountService.notifyChargeFailed(event.getMemberId(), event.getTransactionId(), event.getAmount());
    log.info("Notified FAILED charge to memberId={}", event.getMemberId());
  }

  @EventHandler
  public void on(ChargeCompletedEvent event) {
    accountService.notifyChargeCompleted(event.getMemberId(), event.getTransactionId(), event.getAmount(), event.getTimestamp());
    log.info("Notified COMPLETED charge to memberId={}", event.getMemberId());
  }

  @EventHandler
  public void on(ChargeCreatedEvent event) {
    accountService.notifyChargeCreated(event.getMemberId(), event.getTransactionId(), event.getAmount());
    log.info("Notified CREATED charge to memberId={}", event.getMemberId());
  }
}
