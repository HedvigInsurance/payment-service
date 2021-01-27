package com.hedvig.paymentservice.query.adyenAccount

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class AdyenAccount(
    @Id
    val memberId: String,
    var recurringDetailReference: String,
    var accountStatus: AdyenAccountStatus
)
