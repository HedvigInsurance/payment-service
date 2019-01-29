package com.hedvig.paymentservice.query.registerAccount.enteties

import com.hedvig.paymentservice.domain.registerAccount.enums.RegisterAccountProcessStatus
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class RegisterAccount (
  @Id
  var hedvigOrderId: UUID,
  var memberId: String,
  @Enumerated(EnumType.STRING)
  var status: RegisterAccountProcessStatus
)
