package com.hedvig.paymentservice.services.segmentPublisher

import com.hedvig.paymentservice.domain.payments.events.ChargeFailedEvent
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.segment.analytics.Analytics
import net.logstash.logback.argument.StructuredArguments.value
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*
import javax.money.format.AmountFormatQueryBuilder
import javax.money.format.MonetaryFormats

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroupLive")
class LiveEventListener(
  private val segmentAnalytics: Analytics,
  private val memberRepository: MemberRepository) {

  private val log: Logger = LoggerFactory.getLogger(LiveEventListener::class.java)

  private val integrationSettings = mapOf("All" to false, "Customer.io" to true)

  @EventHandler
  fun on(evt: ChargeFailedEvent) {
    try {
      val member = memberRepository.findById(evt.memberId)
      if (!member.isPresent) {
        return
      }

      member.ifPresentOrElse(
        { m ->
          val transaction = m.getTransaction(evt.transactionId)

          if (transaction == null) {
            log.error("Could not send 'Charge Failed' event transaction not found for member {}, {}", value("memberId", evt.memberId), evt.transactionId)
          } else {

            val money = transaction.money
            val mf = MonetaryFormats.getAmountFormat(
              AmountFormatQueryBuilder.of(Locale("sv", "SE")).set("pattern", "####,##.00").build())

            val properties = mapOf<String, Any>(
              "amount" to mf.format(money),
              "currency" to money.currency.toString())

            segmentAnalytics.track("Charge Failed", properties, m.id, integrationSettings)
          }
        },
        { log.error("Could send 'Charge Failed' event member not found: {} ", value("memberId", evt.memberId)) })

    } catch (e: Exception) {
      log.error("Caught exception when sending 'Charge Failed' event", e)
    }
  }
}
