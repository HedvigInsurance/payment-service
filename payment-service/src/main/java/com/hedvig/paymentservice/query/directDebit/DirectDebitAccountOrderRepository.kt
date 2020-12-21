package com.hedvig.paymentservice.query.directDebit

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DirectDebitAccountOrderRepository : CrudRepository<DirectDebitAccountOrder, UUID> {
    fun findAllByMemberId(memberId: String): List<DirectDebitAccountOrder>

    @Query(
        value = "SELECT DISTINCT ON (member_id) * FROM direct_debit_account_order ORDER BY member_id, created_at DESC",
        nativeQuery = true
    )
    fun findAllWithLatestDirectDebitAccountOrders(memberIds: List<String>): List<DirectDebitAccountOrder>
}
