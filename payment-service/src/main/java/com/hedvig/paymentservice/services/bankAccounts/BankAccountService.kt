package com.hedvig.paymentservice.services.bankAccounts

import com.hedvig.paymentservice.graphQl.types.BankAccount
import com.hedvig.paymentservice.graphQl.types.DirectDebitStatus
import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus
import com.hedvig.paymentservice.query.directDebit.DirectDebitAccountOrder
import com.hedvig.paymentservice.web.dtos.DirectDebitAccountOrderDTO
import java.time.LocalDate

interface BankAccountService {
    fun getBankAccount(memberId: String): BankAccount?
    fun getNextChargeDate(memberId: String): LocalDate?
    fun getDirectDebitStatus(memberId: String): DirectDebitStatus
    fun getPayinMethodStatus(memberId: String): PayinMethodStatus
    fun getLatestDirectDebitAccountOrder(memberId: String): DirectDebitAccountOrderDTO?
}
