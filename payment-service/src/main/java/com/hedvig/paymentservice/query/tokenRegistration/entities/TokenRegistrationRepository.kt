package com.hedvig.paymentservice.query.tokenRegistration.entities

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TokenRegistrationRepository : CrudRepository<TokenRegistration, UUID> {
  fun findByMemberIdOrderByCreatedAt(memberId: String): TokenRegistration?
}
