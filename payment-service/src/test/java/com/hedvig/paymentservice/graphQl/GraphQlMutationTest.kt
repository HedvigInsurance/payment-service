package com.hedvig.paymentservice.graphQl

import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.serviceIntergration.memberService.MemberService
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.trustly.TrustlyService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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

  @Test
  fun registerCard() {
    Mockito.`when`(memberService.getMember(Mockito.any())).thenReturn(Optional.empty())

    graphQLTestTemplate.addHeader("hedvig.token", "123")

    val response = graphQLTestTemplate.perform("/mutations/registerCard.graphql", null)
    val createQuote = response.readTree()["data"]["registerCard"]

    assert(response.isOk)
  }
}
