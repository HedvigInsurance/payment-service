package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccount
import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccountType
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class BookkeepingAccountManager @Autowired constructor(
  private val bookkeepingAccountDao: BookkeepingAccountDao,
  private val memberRepository: MemberRepository
) {
  val log = getLogger(BookkeepingAccountManager::class.java)!!

  fun createAccounts(memberId: String, timestamp: Instant = Instant.now()): List<BookkeepingAccount> {
    val member = memberRepository.findById(memberId)

    if (!member.isPresent) {
      val message = "Member $memberId not found, cannot create accounts"
      log.error(message)
      throw RuntimeException(message)
    }

    val liabilityAccount = BookkeepingAccount(
      id = UUID.randomUUID(),
      member = member.get(),
      type = BookkeepingAccountType.LIABILITY,
      createdAt = timestamp
    )
    val assetAccount = BookkeepingAccount(
      id = UUID.randomUUID(),
      member = member.get(),
      type = BookkeepingAccountType.ASSET,
      createdAt = timestamp
    )

    log.info("Attempting to create new bookkeping accounts [memberId=$memberId, liability=${liabilityAccount.id}, asset=${assetAccount.id}")
    val accounts = listOf(liabilityAccount, assetAccount)
    bookkeepingAccountDao.save(accounts)
    log.info("Successfully created bookeeping accounts [memberId=$memberId, liability=${liabilityAccount.id}, asset=${assetAccount.id}")

    return accounts
  }
}
