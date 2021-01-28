package com.hedvig.paymentservice.query.adyenAccount

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class AdyenAccount(
    @Id
    val memberId: String,
    var recurringDetailReference: String,
    var accountStatus: AdyenAccountStatus
) {
    @field:CreationTimestamp
    @Column(updatable = false)
    lateinit var createdAt: Instant

    @field:UpdateTimestamp
    lateinit var updatedAt: Instant
}
