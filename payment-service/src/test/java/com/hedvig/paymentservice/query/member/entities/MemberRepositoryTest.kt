package com.hedvig.paymentservice.query.member.entities

import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID


@RunWith(SpringRunner::class)
@DataJpaTest
@ContextConfiguration(classes = [PaymentServiceTestConfiguration::class])
class MemberRepositoryTest {

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    lateinit var member: Member

    @Before
    fun setup() {
        member = Member()
        member.id = "kikos"
        val transaction = Transaction()
        transaction.id = UUID.randomUUID()
        transaction.member = member
        member.transactions = hashMapOf(Pair(UUID.randomUUID(), transaction))
        memberRepository.save(member)
    }

    @Test
    fun test(){
        val memberMaybe = memberRepository.findById("kikos")
        val member =  memberMaybe.get()
        assertThat(member.transactions).isNotEmpty
    }

    @Test
    fun delete() {
        memberRepository.deleteAll()
        assertThat(memberRepository.findAll()).isEmpty()
        assertThat(transactionRepository.findAll()).isEmpty()
    }

}
