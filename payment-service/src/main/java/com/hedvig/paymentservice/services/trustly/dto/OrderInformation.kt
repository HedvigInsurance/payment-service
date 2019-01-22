package com.hedvig.paymentservice.services.trustly.dto

import com.hedvig.paymentservice.domain.trustlyOrder.OrderState
import java.util.*


data class OrderInformation (

  val id: UUID,
  val iframeUrl: String,
  val state: OrderState
  )
