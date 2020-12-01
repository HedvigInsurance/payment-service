package com.hedvig.paymentservice.util

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

val chargeDays = mapOf(
    Market.SWEDEN to 27,
    Market.NORWAY to 27,
    Market.DENMARK to 3 // TODO: Set correct danish charge date
)

fun getNextChargeChargeDate(market: Market): LocalDate {
    val today = DateUtil.todayInMarket(market)
    return getNextChargeDateRelativeToDate(market, today)
}

fun getNextChargeDateRelativeToDate(market: Market, date: LocalDate): LocalDate {
    val currentMonth = YearMonth.of(date.year, date.month)
    val chargeDateCurrentPeriod = getChargeDateOfMonth(market, currentMonth)
    return if (!date.isAfter(chargeDateCurrentPeriod)) {
        chargeDateCurrentPeriod
    } else getChargeDateOfMonth(market, currentMonth.plusMonths(1))
}

private fun getChargeDateOfMonth(market: Market, month: YearMonth): LocalDate {
    val chargeDayThisMonth = month.atDay(chargeDays.getValue(market))
    return when (chargeDayThisMonth.dayOfWeek) {
        DayOfWeek.SATURDAY -> chargeDayThisMonth.plusDays(2)
        DayOfWeek.SUNDAY -> chargeDayThisMonth.plusDays(1)
        else -> chargeDayThisMonth
    }
}
