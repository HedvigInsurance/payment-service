package com.hedvig.paymentservice.graphQl

import com.adyen.model.checkout.PaymentsResponse
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.adyen.dtos.AdyenPaymentsResponse
import com.hedvig.paymentservice.services.trustly.TrustlyService
import java.util.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@ContextConfiguration(classes = [PaymentServiceTestConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphQlMutationTest {

  @Autowired
  private lateinit var graphQLTestTemplate: GraphQLTestTemplate

  @MockBean
  private lateinit var trustlyService: TrustlyService

  @MockBean
  private lateinit var adyenService: AdyenService

  @MockBean
  private lateinit var memberService: MemberService

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun tokenizePaymentMethods() {
    Mockito.`when`(memberService.getMember(Mockito.anyString())).thenReturn(Optional.of(makeMember()))

    Mockito.`when`(adyenService.tokenizePaymentDetails(anyObject(), Mockito.anyString(), Mockito.eq("1.1.1.2")))
      .thenAnswer { AdyenPaymentsResponse(PaymentsResponse().resultCode(PaymentsResponse.ResultCodeEnum.AUTHORISED)) }

    graphQLTestTemplate.addHeader("hedvig.token", MEMBER_ID_ONE)
    graphQLTestTemplate.addHeader("x-forwarded-for", "1.1.1.2")

    val response = graphQLTestTemplate.perform("/mutations/registerCard.graphql", null)

    assert(response.isOk)
    assert(response.readTree()["data"]["tokenizePaymentDetails"]["resultCode"].textValue() == "Authorised")
  }

  private fun makeMember() = Member(
    memberId = MEMBER_ID_ONE,
    firstName = "",
    lastName = "",
    birthDate = null,
    street = "",
    zipCode = "",
    ssn = "",
    country = "",
    city = "",
    email = ""
  )

  private fun <T> anyObject(): T {
    return Mockito.any<T>()
  }

  companion object {
    const val MEMBER_ID_ONE = "ONE"
  }
}
