package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccount
import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccountType
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant

class BookkeepingAccountManagerTest {
  @Test
  fun createsNewAccountsWithoutExploding() {
    val bookkeepingAccountDaoStub = mock<BookkeepingAccountDao>()
    val
    val bookkeepingAccountManager = BookkeepingAccountManager(bookkeepingAccountDaoStub)

    val daoArgumentCaptor = argumentCaptor<List<BookkeepingAccount>>()

    val timestamp = Instant.now()
    bookkeepingAccountManager.createAccounts("123", timestamp)

    verify(bookkeepingAccountDaoStub).save(daoArgumentCaptor.capture())

    assertThat(daoArgumentCaptor.firstValue[0].memberId).isEqualTo("123")
    assertThat(daoArgumentCaptor.firstValue[0].type).isEqualTo(BookkeepingAccountType.LIABILITY)
    assertThat(daoArgumentCaptor.firstValue[0].createdAt).isEqualTo(timestamp)

    assertThat(daoArgumentCaptor.firstValue[1].memberId).isEqualTo("123")
    assertThat(daoArgumentCaptor.firstValue[1].type).isEqualTo(BookkeepingAccountType.ASSET)
    assertThat(daoArgumentCaptor.firstValue[1].createdAt).isEqualTo(timestamp)
  }
}
