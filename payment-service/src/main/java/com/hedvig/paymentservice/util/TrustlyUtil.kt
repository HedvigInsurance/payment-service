package com.hedvig.paymentservice.util

import com.hedvig.paymentservice.query.member.entities.Member

fun isUpdateForTheLatestTrustlyAccount(member: Member, trustlyAccountInQuestion: String): Boolean {
    return member.trustlyAccountNumber == null || member.trustlyAccountNumber == trustlyAccountInQuestion
}
