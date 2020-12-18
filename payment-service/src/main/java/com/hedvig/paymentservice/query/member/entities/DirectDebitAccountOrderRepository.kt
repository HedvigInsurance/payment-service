package com.hedvig.paymentservice.query.member.entities

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DirectDebitAccountOrderRepository : CrudRepository<DirectDebitAccountOrder, UUID> {
}
