package com.hedvig.paymentservice.graphQl

import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.RecurringDetail
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.services.adyen.AdyenService
import com.hedvig.paymentservice.services.bankAccounts.BankAccountService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
class GraphQlQueryTest {

  @Autowired
  private lateinit var graphQLTestTemplate: GraphQLTestTemplate

  @MockBean
  private lateinit var bankAccountService: BankAccountService

  @MockBean
  private lateinit var adyenService: AdyenService

  @Test
  fun registerCard() {
    Mockito.`when`(adyenService.getAvailablePaymentMethods())
      .thenReturn(
        PaymentMethodsResponse()
          .addOneClickPaymentMethodsItem(
            RecurringDetail().name("Test")
          )
      )

    graphQLTestTemplate.addHeader("hedvig.token", "123")

    val response = graphQLTestTemplate.perform("/queries/getAvailablePaymentMethods.graphql", null)

    assert(response.isOk)
  }
}
