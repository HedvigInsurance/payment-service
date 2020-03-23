package com.hedvig.paymentservice

import com.hedvig.paymentservice.common.UUIDGenerator
import com.hedvig.paymentservice.common.UUIDGeneratorImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean

@SpringBootApplication(scanBasePackages = ["com.hedvig"])
@EnableFeignClients(basePackages = ["com.hedvig"])
class PaymentServiceApplication {
  @Bean
  fun uuidGenerator(): UUIDGenerator {
    return UUIDGeneratorImpl()
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(PaymentServiceApplication::class.java, *args)
    }
  }
}
