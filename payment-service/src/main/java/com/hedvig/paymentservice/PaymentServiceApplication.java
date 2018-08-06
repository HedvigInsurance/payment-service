package com.hedvig.paymentservice;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.common.UUIDGeneratorImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentServiceApplication {

  @Bean
  UUIDGenerator uuidGenerator() {
    return new UUIDGeneratorImpl();
  }

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
