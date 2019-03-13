package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccount
import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccountType
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BookkeepingAccountRepository : CrudRepository<BookkeepingAccount, UUID> {
  @Query("FROM BookkeepingAccount WHERE member_id = :memberId AND type = :type")
  fun findAccountByMemberAndType(memberId: String, type: BookkeepingAccountType): BookkeepingAccount?
}
