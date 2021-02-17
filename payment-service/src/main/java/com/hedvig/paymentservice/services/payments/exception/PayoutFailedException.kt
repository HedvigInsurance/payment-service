package com.hedvig.paymentservice.services.payments.exception

import org.springframework.http.HttpStatus

class PayoutFailedException(
    override val message: String,
    val httpStatus: HttpStatus
): RuntimeException(message)
