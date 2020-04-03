package com.hedvig.paymentservice.query.adyenTokenRegistration.entities

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdyenTokenRegistrationRepository : CrudRepository<AdyenTokenRegistration, UUID> {
  fun findByMemberIdOrderByCreatedAt(memberId: String): AdyenTokenRegistration?
}
