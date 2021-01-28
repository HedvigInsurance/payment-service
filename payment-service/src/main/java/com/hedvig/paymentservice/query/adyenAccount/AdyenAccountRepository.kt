package com.hedvig.paymentservice.query.adyenAccount

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface AdyenAccountRepository : JpaRepository<AdyenAccount, String>
