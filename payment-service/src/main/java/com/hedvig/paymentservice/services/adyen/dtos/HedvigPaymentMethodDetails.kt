package com.hedvig.paymentservice.services.adyen.dtos

import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodDetails
import com.adyen.model.checkout.PersonalDetails
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class HedvigPaymentMethodDetails(
    @JsonProperty("type")
    private var type: String?,
    @JsonProperty("number")
    val number: String?,
    @JsonProperty("expiryMonth")
    val expiryMonth: String?,
    @JsonProperty("expiryYear")
    val expiryYear: String?,
    @JsonProperty("holderName")
    val holderName: String?,
    @JsonProperty("cvc")
    val cvc: String?,
    @JsonProperty("installmentConfigurationKey")
    val installmentConfigurationKey: String?,
    @JsonProperty("personalDetails")
    val personalDetails: PersonalDetails?,
    @JsonProperty("encryptedCardNumber")
    val encryptedCardNumber: String?,
    @JsonProperty("encryptedExpiryMonth")
    val encryptedExpiryMonth: String?,
    @JsonProperty("encryptedExpiryYear")
    val encryptedExpiryYear: String?,
    @JsonProperty("encryptedSecurityCode")
    val encryptedSecurityCode: String?,
    @JsonProperty("recurringDetailReference")
    val recurringDetailReference: String?,
    @JsonProperty("storeDetails")
    val storeDetails: Boolean?,
    @JsonProperty("issuer")
    val issuer: String?,
    @JsonProperty("sepa.ownerName")
    val sepaOwnerName: String?,
    @JsonProperty("sepa.ibanNumber")
    val sepaIbanNumber: String?,
    @JsonProperty("applepay.token")
    @JsonAlias("applepayToken")
    val applepayToken: String?,
    @JsonProperty("googlePayToken")
    val googlepayToken: String?
) : PaymentMethodDetails {

    override fun getType(): String? {
        return type
    }

    override fun setType(type: String?) {
        this.type = type
    }

    fun toDefaultPaymentMethodDetails(): DefaultPaymentMethodDetails {
        val paymentMethodDetails = DefaultPaymentMethodDetails()

        paymentMethodDetails.type = getType()
        paymentMethodDetails.number = number
        paymentMethodDetails.expiryMonth = expiryMonth
        paymentMethodDetails.expiryYear = expiryYear
        paymentMethodDetails.holderName = holderName
        paymentMethodDetails.cvc = cvc
        paymentMethodDetails.installmentConfigurationKey = installmentConfigurationKey
        paymentMethodDetails.personalDetails = personalDetails
        paymentMethodDetails.encryptedCardNumber = encryptedCardNumber
        paymentMethodDetails.encryptedExpiryMonth = encryptedExpiryMonth
        paymentMethodDetails.encryptedExpiryYear = encryptedExpiryYear
        paymentMethodDetails.encryptedSecurityCode = encryptedSecurityCode
        paymentMethodDetails.recurringDetailReference = recurringDetailReference
        paymentMethodDetails.storeDetails = storeDetails
        paymentMethodDetails.issuer = issuer
        paymentMethodDetails.sepaIbanNumber = sepaIbanNumber
        paymentMethodDetails.sepaOwnerName = sepaOwnerName
        paymentMethodDetails.applepayToken = applepayToken
        paymentMethodDetails.googlepayToken = googlepayToken

        return paymentMethodDetails
    }
}
