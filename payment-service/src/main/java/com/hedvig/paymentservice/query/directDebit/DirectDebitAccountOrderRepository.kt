package com.hedvig.paymentservice.query.directDebit

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DirectDebitAccountOrderRepository : CrudRepository<DirectDebitAccountOrder, UUID> {
    fun findAllByMemberId(memberId: String): List<DirectDebitAccountOrder>
}
