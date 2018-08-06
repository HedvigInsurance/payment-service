package com.hedvig.paymentservice.services.payments;

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TOLVANSSON_MEMBER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRANSACTION_AMOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.payments.commands.CreateChargeCommand;
import com.hedvig.paymentservice.services.payments.dto.ChargeMemberRequest;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

  private static final String REQUEST_ID = "b3c2c1aa-418d-11e8-a94c-eb2ce6a30d41";
  private static final String TRUSTLY_CUSTOMER_INBOX_PATTERN =
      "trustly-customer-inbox\\+(\\d+)@hedvig.com";
  @Mock private CommandGateway gateway;

  @Mock private UUIDGenerator uuidGenerator;

  @Captor private ArgumentCaptor<CreateChargeCommand> captor;

  @Test
  public void
      givenChargeMemberRequest_whenChargeMember_thenSendCreateChargeCommand_WithTrustlyInboxEmail() {
    given(uuidGenerator.generateRandom()).willReturn(UUID.fromString(REQUEST_ID));

    given(gateway.sendAndWait(captor.capture())).willReturn(Boolean.TRUE);

    PaymentService paymentService = new PaymentService(gateway, uuidGenerator);

    paymentService.chargeMember(makeChargeMemberRequest());

    assertThat(captor.getValue().getEmail()).matches(TRUSTLY_CUSTOMER_INBOX_PATTERN);
  }

  private ChargeMemberRequest makeChargeMemberRequest() {
    return new ChargeMemberRequest(TOLVANSSON_MEMBER_ID, TRANSACTION_AMOUNT);
  }
}
