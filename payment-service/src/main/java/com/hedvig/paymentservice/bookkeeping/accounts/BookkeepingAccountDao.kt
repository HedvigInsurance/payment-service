package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccount
import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccountType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class BookkeepingAccountDao @Autowired constructor(val bookkeepingAccountRepository: BookkeepingAccountRepository) {
  fun save(bookkeepingAccounts: List<BookkeepingAccount>) {
    bookkeepingAccountRepository.saveAll(bookkeepingAccounts)
  }
}
