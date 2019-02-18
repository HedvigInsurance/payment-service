package com.hedvig.paymentservice.query.registerAccount.enteties

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRegistrationRepository : CrudRepository<AccountRegistration, UUID> {
  @Query("select a from AccountRegistration a where a.memberId = :memberId")
  fun findByMemberId(@Param("memberId") memberId: String): List<AccountRegistration>
}
