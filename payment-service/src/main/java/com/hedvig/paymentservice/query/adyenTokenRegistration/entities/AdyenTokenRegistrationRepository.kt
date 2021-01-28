package com.hedvig.paymentservice.query.adyenTokenRegistration.entities

import com.hedvig.paymentservice.domain.adyenTokenRegistration.enums.AdyenTokenRegistrationStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdyenTokenRegistrationRepository : CrudRepository<AdyenTokenRegistration, UUID> {
  fun findByMemberId(memberId: String): List<AdyenTokenRegistration>
  fun findByMemberIdAndTokenStatusAndIsForPayoutIsTrue(
      memberId: String,
      tokenStatus: AdyenTokenRegistrationStatus
  ): List<AdyenTokenRegistration>
}
