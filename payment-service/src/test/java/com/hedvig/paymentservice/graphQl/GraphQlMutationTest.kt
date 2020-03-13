package com.hedvig.paymentservice.graphQl

import com.adyen.model.checkout.PaymentsResponse
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.Member
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.trustly.TrustlyService
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
import java.util.Optional

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
  fun tokenizeCard() {
    Mockito.`when`(memberService.getMember(Mockito.any())).thenReturn(Optional.of(makeMember()))

    Mockito.`when`(adyenService.tokenizeCard(anyObject(), Mockito.anyString()))
      .thenAnswer { PaymentsResponse().resultCode(PaymentsResponse.ResultCodeEnum.AUTHORISED) }

    graphQLTestTemplate.addHeader("hedvig.token", MEMBER_ID_ONE)

    val response = graphQLTestTemplate.perform("/mutations/registerCard.graphql", null)

    assert(response.isOk)
    assert(response.readTree()["data"]["tokenizeCard"].textValue().contains("AUTHORISED"))
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
    city = ""
  )

  private fun <T> anyObject(): T {
    return Mockito.any<T>()
  }

  companion object {
    const val MEMBER_ID_ONE = "ONE"
  }
}
