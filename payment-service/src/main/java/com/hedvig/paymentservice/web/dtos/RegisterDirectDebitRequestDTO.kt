package com.hedvig.paymentservice.web.dtos

import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext
import javax.annotation.Nullable

data class RegisterDirectDebitRequestDTO(
    val firstName: String,
    val lastName: String,
    val personalNumber: String,
    @Nullable val clientContext: RegisterDirectDebitClientContext?
)
