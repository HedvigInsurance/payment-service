package com.hedvig.paymentservice.domain;

import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class DomainTestUtilities {
  public static Matcher<List<? extends DomainEventMessage<?>>> hasEvent(final Class<?> c) {
    return new TypeSafeMatcher<List<? extends DomainEventMessage<?>>>() {
      @Override
      public boolean matchesSafely(final List<? extends DomainEventMessage<?>> eventList) {
        val matches =
            eventList
                .stream()
                .filter(e -> e.getPayload().getClass().equals(c))
                .collect(Collectors.toList());
        return matches.size() != 0;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("List should contain event of type: ").appendValue(c);
      }
    };
  }
}
