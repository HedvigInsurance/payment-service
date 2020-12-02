package com.hedvig.paymentservice.util

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ChargeUtilTest {
    @Test
    internal fun `calculates the correct charge date of regular month when asking before charge date`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 10, 27)
        val today = LocalDate.of(2020, 10, 1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 10, 27))
    }

    @Test
    internal fun `calculates the correct charge date of month with charge date on sunday`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 9, 27)
        val today = LocalDate.of(2020, 9, 1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 9, 28))
    }

    @Test
    internal fun `calculates the correct charge date of month with charge date on saturday`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 6, 27)
        val today = LocalDate.of(2020, 6, 1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 6, 29))
    }

    @Test
    internal fun `returns today when asking for charge on the charge date`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 10, 27)
        val today = chargeDayOfThatMonth // Same day
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(today)
    }

    @Test
    internal fun `returns tomorrow when asking for charge on the charge day but it is a sunday`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 9, 27)
        val today = chargeDayOfThatMonth // Same day
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 9, 28))
    }

    @Test
    internal fun `returns next month's charge date when asking after the charge date`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 10, 27)
        val today = chargeDayOfThatMonth.plusDays(1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.SWEDEN, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 11, 27))
    }

    @Test
    internal fun `calculates the correct charge date of regular month in Denmark`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 9, 1)
        val today = LocalDate.of(2020, 9, 1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.DENMARK, today)
        assertThat(chargeDate).isEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 9, 1))
    }

    @Test
    internal fun `calculates the correct charge date when charge day is saturday in Denmark`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 11, 1)
        val today = LocalDate.of(2020, 11, 1)
        val chargeDate = getNextChargeDateRelativeToDate(Market.DENMARK, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 11, 2))
    }

    @Test
    internal fun `returns the next charge date of regular month when asking after current months charge date in Denmark`() {
        val chargeDayOfThatMonth = LocalDate.of(2020, 10, 1)
        val today = LocalDate.of(2020, 10, 10)
        val chargeDate = getNextChargeDateRelativeToDate(Market.DENMARK, today)
        assertThat(chargeDate).isNotEqualTo(chargeDayOfThatMonth)
        assertThat(chargeDate).isEqualTo(LocalDate.of(2020, 11, 2))
    }
}
