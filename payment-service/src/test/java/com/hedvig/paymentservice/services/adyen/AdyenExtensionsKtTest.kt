package com.hedvig.paymentservice.services.adyen

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class AdyenExtensionsKtTest {

  @Test
  fun `money of 10 SEK should return long of 1000 from adyen minor units`() {
    val tenSek = Money.of(BigDecimal.TEN, "SEK")

    val result = tenSek.toAdyenMinorUnits()

    assertThat(result).isEqualTo(1000)
  }

  @Test
  fun `money of 88,88 NOK should return long of 8888 from adyen minor units`() {
    val tenNok = Money.of(BigDecimal("88.88"), "NOK")

    val result = tenNok.toAdyenMinorUnits()

    assertThat(result).isEqualTo(8888)
  }

  @Test
  fun `money of 10 TND should then throws RuntimeException`() {
    val tenTnd = Money.of(BigDecimal.TEN, "TND")

    assertThrows<RuntimeException> {
      tenTnd.toAdyenMinorUnits()
    }
  }
}
