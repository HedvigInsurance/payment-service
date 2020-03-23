package com.hedvig.paymentservice.graphQl

import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.RecurringDetail
import com.graphql.spring.boot.test.GraphQLTestTemplate
import com.hedvig.paymentservice.PaymentServiceTestConfiguration
import com.hedvig.paymentservice.graphQl.types.ActivePaymentMethodsResponse
import com.hedvig.paymentservice.graphQl.types.AvailablePaymentMethodsResponse
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
  fun availablePaymentMethods() {
    Mockito.`when`(adyenService.getAvailablePaymentMethods())
      .thenReturn(
        AvailablePaymentMethodsResponse(
          PaymentMethodsResponse()
            .addOneClickPaymentMethodsItem(
              RecurringDetail().name("Test")
            )
        )
      )

    graphQLTestTemplate.addHeader("hedvig.token", "123")

    val response = graphQLTestTemplate.perform("/queries/availablePaymentMethods.graphql", null)

    assert(response.isOk)
    assert(response.readTree()["data"]["availablePaymentMethods"].toString().contains("Test"))
  }

  @Test
  fun activePaymentMethods() {
    Mockito.`when`(adyenService.getActivePaymentMethods(Mockito.anyString()))
      .thenReturn(
        ActivePaymentMethodsResponse(
          PaymentMethodsResponse()
            .addOneClickPaymentMethodsItem(
              RecurringDetail().name("Test")
            )
        )
      )

    graphQLTestTemplate.addHeader("hedvig.token", "123")

    val response = graphQLTestTemplate.perform("/queries/activePaymentMethods.graphql", null)

    assert(response.isOk)
    assert(response.readTree()["data"]["activePaymentMethods"].toString().contains("Test"))
  }
}
