package com.hedvig.paymentservice.query.adyenAccount

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface AdyenAccountRepository : CrudRepository<AdyenAccount, String>
