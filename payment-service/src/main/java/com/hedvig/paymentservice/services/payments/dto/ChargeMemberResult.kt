package com.hedvig.paymentservice.services.payments.dto

import java.util.UUID


class ChargeMemberResult(
  var transactionId: UUID,
  var type: ChargeMemberResultType
)
