package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/_/transactions/")
public class TransactionController {

  private final TransactionRepository repository;

  public TransactionController(TransactionRepository repository) {
    this.repository = repository;
  }

  @GetMapping("{transactionId}")
  public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
    Optional<Transaction> optionalTransaction = repository.findById(transactionId);

    return optionalTransaction.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
