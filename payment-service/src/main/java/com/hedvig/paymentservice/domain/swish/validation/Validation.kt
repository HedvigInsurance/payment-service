package com.hedvig.paymentservice.domain.swish.validation

private val validMessageRegex = Regex("^[a-zA-Z0-9]*$")

fun String.isValidSwishMessage() =
    this.length <= 50 && this.matches(validMessageRegex)
