package com.hedvig.paymentservice.bookkeeping.entries

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BookkeepingEntryRepository : CrudRepository<BookkeepingEntry, UUID>
