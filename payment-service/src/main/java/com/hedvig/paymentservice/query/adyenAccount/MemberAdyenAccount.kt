package com.hedvig.paymentservice.query.adyenAccount

import com.hedvig.paymentservice.domain.payments.enums.AdyenAccountStatus
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
data class MemberAdyenAccount(
    @Id
    val memberId: String,
    var merchantAccount: String
) {
    var recurringDetailReference: String? = null

    @Enumerated(EnumType.STRING)
    var accountStatus: AdyenAccountStatus? = null

    @field:CreationTimestamp
    @Column(updatable = false)
    lateinit var createdAt: Instant

    @field:UpdateTimestamp
    lateinit var updatedAt: Instant
}
