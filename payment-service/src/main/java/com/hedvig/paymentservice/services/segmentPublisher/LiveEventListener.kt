package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.segment.analytics.Analytics
import org.axonframework.config.ProcessingGroup
import org.springframework.stereotype.Component
import java.util.*
import javax.money.format.AmountFormatQueryBuilder
import javax.money.format.MonetaryFormats

@Component
@ProcessingGroup("SegmentProcessorGroupLive")
open class LiveEventListener(
  private val segmentAnalytics: Analytics,
  private val memberRepository: MemberRepository) {

  fun on(evt:ChargeFailedEvent){
    val member = memberRepository.findById(evt.memberId)
    if(member.isPresent) {
      val m = member.get()
      val transaction = m.getTransaction(evt.transactionId)

      if(transaction != null) {
        val money = transaction.money
        val mf = MonetaryFormats.getAmountFormat(
          AmountFormatQueryBuilder.of(Locale("sv", "SE")).set("pattern", "####,##.00").build())

        val properties = mapOf<String, Any>(
          "amount" to mf.format(money),
          "currency" to transaction.currency)

        segmentAnalytics.track("Charge Failed", properties, m.id)
      }
    }
  }
}
