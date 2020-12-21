package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.query.member.entities.TransactionRepository;
import com.hedvig.paymentservice.web.dtos.TransactionDTO;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/_/transactions/")
public class TransactionController {

  private final TransactionRepository repository;

  public TransactionController(TransactionRepository repository) {
    this.repository = repository;
  }

  @GetMapping("{transactionId}")
  public ResponseEntity<TransactionDTO> getTransaction(@PathVariable UUID transactionId) {
    Optional<Transaction> optionalTransaction = repository.findById(transactionId);

    return optionalTransaction
        .map(transaction -> ResponseEntity.ok(TransactionDTO.Companion.fromTransaction(transaction)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
