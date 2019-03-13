package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.domain.payments.events.MemberCreatedEvent
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BookkeepingAccountEventListener @Autowired constructor(private val bookkeepingAccountManager: BookkeepingAccountManager) {
  @EventHandler
  fun onMemberCreated(e: MemberCreatedEvent, @Timestamp timestamp: Instant) {
    bookkeepingAccountManager.createAccounts(e.memberId, timestamp)
  }
}
