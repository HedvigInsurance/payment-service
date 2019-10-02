package com.hedvig.paymentservice.services.trustly.exceptions

class InvalidRedirectException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: RuntimeException) : super(message, cause)
}
