package com.hedvig.paymentservice.serviceIntergration.productPricing

import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.Transaction
import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.PolicyGuessRequestDto
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import java.time.Instant
import java.time.YearMonth
import java.util.ArrayList
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.http.ResponseEntity

class ProductPricingServiceImplTest {
  @MockK
  lateinit var productPricingClient: ProductPricingClient

  lateinit var service: ProductPricingService

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    service = ProductPricingServiceImpl(productPricingClient)
  }

  @Test
  fun testGetsPolicyGuesses() {
    val member = Member()
    member.id = "123"

    val period = YearMonth.of(2019, 2)

    val transaction1 = Transaction()
    val transaction2 = Transaction()

    transaction1.id = UUID.randomUUID()
    transaction1.timestamp = Instant.now()
    transaction1.setAmount(BigDecimal.TEN)
    transaction1.setCurrency("SEK")
    transaction1.member = member

    transaction2.id = UUID.randomUUID()
    transaction2.timestamp = Instant.now()
    transaction2.setAmount(BigDecimal.ONE)
    transaction2.setCurrency("SEK")
    transaction2.member = member

    val transactions: MutableList<Transaction> = ArrayList()
    transactions.add(transaction1)
    transactions.add(transaction2)

    val slot = slot<Collection<PolicyGuessRequestDto>>()

    every {
      productPricingClient.guessPolicyTypes(
        capture(slot),
        eq(period)
      )
    } returns ResponseEntity.ok(emptyMap())

    service.guessPolicyTypes(transactions, period)

    verify(exactly = 1) { productPricingClient.guessPolicyTypes(any(), period) }

    val captured = slot.captured.toList()

    assertThat(transaction1.id).isEqualTo(captured[0].id)
    assertThat(transaction2.id).isEqualTo(captured[1].id)

    confirmVerified(productPricingClient)
  }
}
