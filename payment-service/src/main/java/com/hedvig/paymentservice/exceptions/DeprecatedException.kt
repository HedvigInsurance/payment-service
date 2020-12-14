package com.hedvig.paymentservice.exceptions

class DeprecatedException(className: String, functionName: String, parameters: List<Any?>) : RuntimeException(
    "Deprecated: Attempted to call function $functionName on $className with parameters: $parameters"
)
