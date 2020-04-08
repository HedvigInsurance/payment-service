package com.hedvig.paymentservice.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {
  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http
      .authorizeRequests()
      .antMatchers("/hooks/adyen/*").authenticated()
      .anyRequest().permitAll()
      .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .httpBasic()
      .and()
      .csrf().disable()
  }
}
