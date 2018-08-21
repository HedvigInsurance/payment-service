package com.hedvig.paymentservice.web;

import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LogRequestFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      // Setup MDC data:

      if (HttpServletRequest.class.isInstance(request)) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String hedvigToken = httpRequest.getHeader("hedvig.token");
        if (hedvigToken != null) {
          MDC.put("memberId", hedvigToken);
          Sentry.getContext().setUser(new UserBuilder().setId(hedvigToken).build());
        }
      }
      chain.doFilter(request, response);
    } finally {
      // Tear down MDC data:
      // ( Important! Cleans up the ThreadLocal data again )
      MDC.clear();
    }
  }

  @Override
  public void destroy() {}
}
