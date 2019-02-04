package com.hedvig.paymentservice.query.registerAccount.enteties

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRegistrationRepository : CrudRepository<AccountRegistration, UUID>
