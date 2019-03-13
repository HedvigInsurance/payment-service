package com.hedvig.paymentservice.bookkeeping.entities

/**
 * For liability accounts: credit = we owe the member, debit = the member owes us
 * For asset accounts: credit = the member paid us, debit = we paid the member
 */
enum class BookkeepingEntryType {
  CREDIT,
  DEBIT
}
