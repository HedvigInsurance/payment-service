package com.hedvig.paymentservice.web.dtos

data class SelectAccountDTO(
    val firstName: String?,
    val lastName: String?,
    val ssn: String?,
    val email: String?,
    val memberId: String?,
    val requestId: String?
)
