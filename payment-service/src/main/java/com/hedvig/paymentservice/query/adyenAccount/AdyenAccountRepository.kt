package com.hedvig.paymentservice.query.adyenAccount

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface AdyenAccountRepository : JpaRepository<MemberAdyenAccount, String> {
    fun findAllByMemberIdIn(ids: List<String>): List<MemberAdyenAccount>
}
