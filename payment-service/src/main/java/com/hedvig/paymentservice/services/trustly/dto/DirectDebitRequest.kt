package com.hedvig.paymentservice.services.trustly.dto

import com.hedvig.paymentservice.graphQl.types.RegisterDirectDebitClientContext
import javax.annotation.Nullable

class DirectDebitRequest(
  val firstName: String,
  val lastName: String,
  val ssn: String,
  val memberId: String,
  val triggerId: String,
  @Nullable val clientContext: RegisterDirectDebitClientContext?
)
