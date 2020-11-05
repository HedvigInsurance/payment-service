package com.hedvig.paymentservice.services.bankAccounts

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class BankAccountServiceImplTest {
  @Test
  internal fun `calculates the correct charge date of regular month`() {
    val regularPeriod = YearMonth.of(2020, 10)
    val chargeDate = BankAccountServiceImpl.getChargeDateOfPeriod(regularPeriod)
    assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 10, 27))
  }

  @Test
  internal fun `calculates the correct charge date of month with charge date on sunday`() {
    val regularPeriod = YearMonth.of(2020, 9)
    val chargeDate = BankAccountServiceImpl.getChargeDateOfPeriod(regularPeriod)
    assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 9, 28))
  }

  @Test
  internal fun `calculates the correct charge date of month with charge date on saturdayy`() {
    val regularPeriod = YearMonth.of(2020, 6)
    val chargeDate = BankAccountServiceImpl.getChargeDateOfPeriod(regularPeriod)
    assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 6, 29))
  }
}
