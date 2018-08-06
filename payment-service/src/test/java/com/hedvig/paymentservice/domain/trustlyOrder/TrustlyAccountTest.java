package com.hedvig.paymentservice.domain.trustlyOrder;

import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateAccountCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.events.TrustlyAccountCreatedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyAccountTest {

  public static final String ACCOUNT_ID = "123456";
  private FixtureConfiguration<TrustlyAccount> fixture;

  @Before
  public void setUp() {
    fixture = new AggregateTestFixture<>(TrustlyAccount.class);
  }

  @Test
  public void testAccountCreation() {
    fixture
        .given()
        .when(new CreateAccountCommand(ACCOUNT_ID))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new TrustlyAccountCreatedEvent(ACCOUNT_ID));
  }
}
