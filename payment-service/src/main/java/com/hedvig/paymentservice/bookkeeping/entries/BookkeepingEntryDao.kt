package com.hedvig.paymentservice.bookkeeping.entries

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BookkeepingEntryDao @Autowired constructor(private val bookkeepingEntryRepository: BookkeepingEntryRepository) {
  fun save(bookkeepingEntry: BookkeepingEntry) {
    bookkeepingEntryRepository.save(bookkeepingEntry);
  }
}
