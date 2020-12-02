package com.hedvig.paymentservice.util

import com.hedvig.paymentservice.serviceIntergration.productPricing.dto.Market
import java.time.LocalDate
import java.time.ZoneId

object DateUtil {

    private val SWEDEN_ZONE_ID = ZoneId.of("Europe/Stockholm")
    private val NORWAY_ZONE_ID = ZoneId.of("Europe/Oslo")
    private val DENMARK_ZONE_ID = ZoneId.of("Europe/Copenhagen")

    private fun todayInSweden() = LocalDate.now(SWEDEN_ZONE_ID)
    private fun todayInNorway() = LocalDate.now(NORWAY_ZONE_ID)
    private fun todayInDenmark() = LocalDate.now(DENMARK_ZONE_ID)

    fun todayInMarket(market: Market) = when (market) {
        Market.SWEDEN -> todayInSweden()
        Market.NORWAY -> todayInNorway()
        Market.DENMARK -> todayInDenmark()
    }
}
