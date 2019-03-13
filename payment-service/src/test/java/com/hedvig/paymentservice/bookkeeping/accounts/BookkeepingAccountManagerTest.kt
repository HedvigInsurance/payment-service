package com.hedvig.paymentservice.bookkeeping.accounts

import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccount
import com.hedvig.paymentservice.bookkeeping.entities.BookkeepingAccountType
import com.hedvig.paymentservice.query.member.entities.Member
import com.hedvig.paymentservice.query.member.entities.MemberRepository
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant
import java.util.*

class BookkeepingAccountManagerTest {
  @Test
  fun createsNewAccountsWithoutExploding() {
    val member = Member()
    member.id = "123"
    val bookkeepingAccountDaoStub = mock<BookkeepingAccountDao>()
    val memberRepository = mock<MemberRepository> {
      on { findById(eq("123")) } doReturn Optional.of(member)
    }
    val bookkeepingAccountManager = BookkeepingAccountManager(bookkeepingAccountDaoStub, memberRepository)

    val daoArgumentCaptor = argumentCaptor<List<BookkeepingAccount>>()

    val timestamp = Instant.now()
    bookkeepingAccountManager.createAccounts("123", timestamp)

    verify(bookkeepingAccountDaoStub).save(daoArgumentCaptor.capture())

    assertThat(daoArgumentCaptor.firstValue[0].member.id).isEqualTo("123")
    assertThat(daoArgumentCaptor.firstValue[0].type).isEqualTo(BookkeepingAccountType.LIABILITY)
    assertThat(daoArgumentCaptor.firstValue[0].createdAt).isEqualTo(timestamp)

    assertThat(daoArgumentCaptor.firstValue[1].member.id).isEqualTo("123")
    assertThat(daoArgumentCaptor.firstValue[1].type).isEqualTo(BookkeepingAccountType.ASSET)
    assertThat(daoArgumentCaptor.firstValue[1].createdAt).isEqualTo(timestamp)
  }
}
