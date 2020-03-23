package com.hedvig.paymentservice.query.adyen.entities

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdyenTokenRepository : CrudRepository<AdyenToken, UUID>
