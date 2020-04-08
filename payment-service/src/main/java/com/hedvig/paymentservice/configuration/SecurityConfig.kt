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
      .csrf().disable()
      .httpBasic().and()
      .authorizeRequests()
      .antMatchers("/**").permitAll()
      .antMatchers("/hooks/adyen/**").authenticated()
      .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
  }
}
