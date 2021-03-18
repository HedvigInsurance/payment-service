package com.hedvig.paymentservice.web

import com.hedvig.paymentservice.services.payments.exception.PayoutFailedException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(value = [PayoutFailedException::class])
    fun handleException(exception: PayoutFailedException, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity.status(exception.httpStatus).build()
    }

}
