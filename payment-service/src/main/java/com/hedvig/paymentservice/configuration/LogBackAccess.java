package com.hedvig.paymentservice.configuration;

import ch.qos.logback.access.tomcat.LogbackValve;
import javax.servlet.Filter;
import lombok.val;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogBackAccess {

  @Bean(name = "TeeFilter")
  public Filter teeFilter() {
    return new ch.qos.logback.access.servlet.TeeFilter();
  }

  @Bean
  public ServletWebServerFactory servletContainer() {
    val tomcat = new TomcatServletWebServerFactory();

    LogbackValve logbackValve = new LogbackValve();

    // point to logback-access.xml
    logbackValve.setFilename("logback-access.xml");

    tomcat.addContextValves(logbackValve);

    return tomcat;
  }
}
