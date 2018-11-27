package com.hedvig.paymentservice.configuration;

import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.jackson.datatype.money.MoneyModule;

@Configuration
public class Money {
  @Bean
  public Module monetaModule() {
    return new MoneyModule().withQuotedDecimalNumbers();
  }
}
