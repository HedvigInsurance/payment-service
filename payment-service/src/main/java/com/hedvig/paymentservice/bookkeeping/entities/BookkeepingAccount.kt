package com.hedvig.paymentservice.bookkeeping.entities

import com.hedvig.paymentservice.query.member.entities.Member
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "member_bookkeeping_account_type", columnNames = ["member_id", "type"])])
data class BookkeepingAccount(
  @Id
  val id: UUID,

  @ManyToOne
  val member: Member,

  val type: BookkeepingAccountType,

  val createdAt: Instant
)
