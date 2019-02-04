package com.hedvig.paymentservice.query.registerAccount.enteties

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class AccountRegistration (
  @Id
  var accountRegistrationId: UUID,
  var memberId: String,
  @Enumerated(EnumType.STRING)
  var status: AccountRegistrationStatus
)
