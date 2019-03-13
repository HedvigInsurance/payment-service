package com.hedvig.paymentservice.bookkeeping.entities

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class BookkeepingEntry(
  @Id
  val id: UUID,

  @ManyToOne
  val bookkeepingAccount: BookkeepingAccount,

  val type: BookkeepingEntryType,

  val reversesEntry: UUID?,

  val balances: UUID?,

  val amount: BigDecimal,

  val reference: String,

  val source: BookkeepingEntrySource,

  val madeByHedvigStaff: String?,

  val createdAt: Instant
)
