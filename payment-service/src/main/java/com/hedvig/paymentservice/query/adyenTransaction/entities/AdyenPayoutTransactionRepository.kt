package com.hedvig.paymentservice.query.adyenTransaction.entities

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdyenPayoutTransactionRepository : CrudRepository<AdyenPayoutTransaction, UUID>
