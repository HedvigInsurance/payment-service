package com.hedvig.paymentservice.web.internal;

import com.hedvig.paymentservice.services.trustly.TrustlyService;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentservice.web.dtos.SelectAccountDTO;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/trustlyOrder/")
public class TrustlyController {

  private final TrustlyService service;

  public TrustlyController(TrustlyService service) {
    this.service = service;
  }

  @GetMapping("/registerDirectDebit")
  public ResponseEntity<SelectAccountDTO> getRegisterDirectDebit() {

    return ResponseEntity.ok(new SelectAccountDTO("", "", "", "", "", ""));
  }

  @PostMapping("/registerDirectDebit")
  public ResponseEntity<DirectDebitResponse> postRegisterDirectDebit(
      @RequestBody DirectDebitRequest requestData) {
    final DirectDebitResponse directDebitResponse = service.requestDirectDebitAccount(requestData);

    return ResponseEntity.ok(directDebitResponse);
  }

  @GetMapping("{orderId}")
  public ResponseEntity<OrderInformation> orderInformation(@PathVariable UUID orderId) {
    OrderInformation order = service.orderInformation(orderId);

    return ResponseEntity.ok(order);
  }
}
