package com.hedvig.paymentservice.query.adyenTransaction.entities

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AdyenPayoutTransactionRepository : CrudRepository<AdyenPayoutTransaction, UUID>
