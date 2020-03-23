package com.hedvig.paymentservice.query.adyen.entities

import com.adyen.model.checkout.PaymentsResponse
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class AdyenToken(
  @Id
  val adyenTokenId: UUID,
  val memberId: String
) {
  var recurringDetailReference: String? = null
  @Enumerated(EnumType.STRING)
  var tokenStatus: PaymentsResponse.ResultCodeEnum? = null
  @CreationTimestamp
  lateinit var createdAt: Instant
}
