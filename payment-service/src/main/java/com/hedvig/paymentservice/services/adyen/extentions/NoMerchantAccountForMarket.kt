package com.hedvig.paymentservice.services.adyen.extentions

import com.hedvig.paymentservice.services.adyen.util.AdyenMerchantPicker

class NoMerchantAccountForMarket(
    market: AdyenMerchantPicker.Market
) : Exception("Cannot fetch merchant account for ${market.name}")
