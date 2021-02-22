package com.hedvig.paymentservice.services.adyen.dtos

import com.adyen.model.checkout.StoredPaymentMethod

data class StoredPaymentMethodsDetails(
    val id: String,
    val cardName: String,
    val brand: String,
    val lastFourDigits: String,
    val expiryMonth: String,
    val expiryYear: String,
    val holderName: String
) {
    companion object {
        fun from(s: StoredPaymentMethod): StoredPaymentMethodsDetails {
            return StoredPaymentMethodsDetails(
                id = s.id,
                cardName = s.name,
                brand = s.brand,
                lastFourDigits = s.lastFour,
                expiryMonth = s.expiryMonth,
                expiryYear = s.expiryYear,
                holderName = s.holderName
            )
        }
    }
}
