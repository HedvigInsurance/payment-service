package com.hedvig.paymentservice.query.registerAccount.enteties

import java.time.Instant
import java.util.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AccountRegistrationRepository : CrudRepository<AccountRegistration, UUID> {
  @Query("select a from AccountRegistration a where a.memberId = :memberId")
  fun findByMemberId(@Param("memberId") memberId: String): List<AccountRegistration>

  @Query("select a from AccountRegistration a where a.initiated < :date and a.status = 'REQUESTED'")
  fun findRequestedRegistrationByDate(@Param("date") date: Instant): List<AccountRegistration>
}
