package com.hedvig.paymentservice.graphQl.configurations

import com.adyen.model.checkout.PaymentMethodsResponse
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.graphql.commons.scalars.LocalDateScalar
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfiguration(
  val objectMapper: ObjectMapper
) {
  @Bean
  fun paymentMethodsResponseScalar(): GraphQLScalarType {
    return GraphQLScalarType.newScalar()
      .name("PaymentMethodsResponse")
      .description("A representation of Adyen's PaymentMethodsResponse")
      .coercing(object : Coercing<PaymentMethodsResponse, String> {

        @Throws(CoercingSerializeException::class)
        override fun serialize(dataFetcherResult: Any?): String? {
          if (dataFetcherResult == null) {
            return null
          }

          if (dataFetcherResult !is PaymentMethodsResponse) {
            throw CoercingSerializeException(
              "dataFetcherResult is of wrong type: " +
                "Expected {${PaymentMethodsResponse::class.java.simpleName}}, got {${dataFetcherResult.javaClass.simpleName}}"
            )
          }

          return objectMapper.writeValueAsString(dataFetcherResult)
        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(input: Any): PaymentMethodsResponse {
          try {
            return objectMapper.readValue(input as String, PaymentMethodsResponse::class.java)
          } catch (e: Exception) {
            throw CoercingParseValueException("Could not parse value $input [Exception: $e]")
          }
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(input: Any): PaymentMethodsResponse {
          return try {
            objectMapper.readValue((input as StringValue).value, PaymentMethodsResponse::class.java)
          } catch (e: Exception) {
            throw CoercingParseLiteralException("Could not parse value $input [Exception: $e]")
          }
        }
      }).build()
  }

  @Bean
  fun paymentsRequestScalar(): GraphQLScalarType {
    return GraphQLScalarType.newScalar()
      .name("PaymentsRequest")
      .description("A representation of Adyen's payments request")
      .coercing(object : Coercing<PaymentsRequest, String> {
        @Throws(CoercingSerializeException::class)
        override fun serialize(dataFetcherResult: Any?): String? {
          if (dataFetcherResult == null) {
            return null
          }

          if (dataFetcherResult !is PaymentsRequest) {
            throw CoercingSerializeException(
              "dataFetcherResult is of wrong type: " +
                "Expected {${PaymentsRequest::class.java.simpleName}}, got {${dataFetcherResult.javaClass.simpleName}}"
            )
          }

          return objectMapper.writeValueAsString(dataFetcherResult)
        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(input: Any): PaymentsRequest {
          try {
            return objectMapper.readValue(input as String, PaymentsRequest::class.java)
          } catch (e: Exception) {
            throw CoercingParseValueException("Could not parse value $input [Exception: $e]")
          }
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(input: Any): PaymentsRequest {
          return try {
            objectMapper.readValue((input as StringValue).value, PaymentsRequest::class.java)
          } catch (e: Exception) {
            throw CoercingParseLiteralException("Could not parse value $input [Exception: $e]")
          }
        }
      }).build()
  }

  @Bean
  fun paymentsResponseScalar(): GraphQLScalarType {
    return GraphQLScalarType.newScalar()
      .name("PaymentsResponse")
      .description("A representation of Adyen's payments response")
      .coercing(object : Coercing<PaymentsResponse, String> {
        @Throws(CoercingSerializeException::class)
        override fun serialize(dataFetcherResult: Any?): String? {
          if (dataFetcherResult == null) {
            return null
          }

          if (dataFetcherResult !is PaymentsResponse) {
            throw CoercingSerializeException(
              "dataFetcherResult is of wrong type: " +
                "Expected {${PaymentsResponse::class.java.simpleName}}, got {${dataFetcherResult.javaClass.simpleName}}"
            )
          }

          return objectMapper.writeValueAsString(dataFetcherResult)
        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(input: Any): PaymentsResponse {
          try {
            return objectMapper.readValue(input as String, PaymentsResponse::class.java)
          } catch (e: Exception) {
            throw CoercingParseValueException("Could not parse value $input [Exception: $e]")
          }
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(input: Any): PaymentsResponse {
          return try {
            objectMapper.readValue((input as StringValue).value, PaymentsResponse::class.java)
          } catch (e: Exception) {
            throw CoercingParseLiteralException("Could not parse value $input [Exception: $e]")
          }
        }
      }).build()
  }

  @Bean
  fun localDateScalar(): GraphQLScalarType {
    return LocalDateScalar()
  }

}
