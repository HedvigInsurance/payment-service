package com.hedvig.paymentservice.serviceIntergration.productPricing;

import com.hedvig.paymentservice.query.member.entities.Member;
import com.hedvig.paymentservice.query.member.entities.Transaction;
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProductPricingServiceImplTest {
  @Test
  public void testGetsPolicyGuesses() {
    final Member member = mock(Member.class);
    final Transaction transaction1 = mock(Transaction.class);
    final Transaction transaction2 = mock(Transaction.class);

    when(member.getId()).thenReturn("123");

    when(transaction1.getId()).thenReturn(UUID.randomUUID());
    when(transaction1.getTimestamp()).thenReturn(Instant.now());
    when(transaction1.getMoney()).thenReturn(Money.of(BigDecimal.TEN, "SEK"));
    when(transaction1.getMember()).thenReturn(member);

    when(transaction2.getId()).thenReturn(UUID.randomUUID());
    when(transaction2.getTimestamp()).thenReturn(Instant.now());
    when(transaction2.getMoney()).thenReturn(Money.of(BigDecimal.ONE, "SEK"));
    when(transaction2.getMember()).thenReturn(member);

    final List<Transaction> transactions = new ArrayList<>();
    transactions.add(transaction1);
    transactions.add(transaction2);

    final ProductPricingClient clientStub = mock(ProductPricingClient.class);
    final ProductPricingService service = new ProductPricingServiceImpl(clientStub);

    final ArgumentCaptor<List<PolicyGuessRequestDto>> captor = ArgumentCaptor.forClass(List.class);
    when(clientStub.guessPolicyTypes(any())).thenReturn(mock(ResponseEntity.class));
    service.guessPolicyTypes(transactions);

    verify(clientStub).guessPolicyTypes(captor.capture());
    assertEquals(transaction1.getId(), captor.getValue().get(0).getId());
    assertEquals(transaction2.getId(), captor.getValue().get(1).getId());
  }
}
