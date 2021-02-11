package com.hedvig.paymentservice.serviceIntergration.memberService.dto

import java.time.LocalDate

data class Member(
    val memberId: String,

    val firstName: String,
    val lastName: String,

    val birthDate: LocalDate?,

    val street: String?,
    val city: String?,
    val zipCode: String?,
    val country: String?,

    val ssn: String,

    val email: String
)
