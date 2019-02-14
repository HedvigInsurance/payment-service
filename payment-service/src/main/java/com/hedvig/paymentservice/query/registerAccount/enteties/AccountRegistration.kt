package com.hedvig.paymentservice.query.registerAccount.enteties

import com.hedvig.paymentservice.domain.accountRegistration.enums.AccountRegistrationStatus
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
class AccountRegistration (
  @Id
  var accountRegistrationId: UUID,
  var memberId: String,
  @Enumerated(EnumType.STRING)
  var status: AccountRegistrationStatus,
  var hedvigOrderId: UUID,
  var initiated: Instant
)
