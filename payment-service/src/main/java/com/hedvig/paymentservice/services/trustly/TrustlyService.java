package com.hedvig.paymentservice.services.trustly;


import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyAPIException;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyConnectionException;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentService.trustly.requestbuilders.SelectAccount;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateTrustlySelectAccountOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceviedCommand;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.response.Response;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TrustlyService {

    public static final String NOTIFICATION_URL = "https://google.com";
    public static final String COUNTRY = "SE";
    private final Logger log = LoggerFactory.getLogger(TrustlyService.class);
    private final SignedAPI api;
    private final CommandGateway gateway;
    private final UUIDGenerator uuidGenerator;
    private final String successUrl;
    private final String failUrl;

    public TrustlyService(SignedAPI api, CommandGateway gateway, UUIDGenerator uuidGenerator, @Value("${hedvig.trustly.successURL}") String successUrl, @Value("${hedvig.trustly.failURL}") String failUrl) {
        this.api = api;
        this.gateway = gateway;
        this.uuidGenerator = uuidGenerator;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
    }

    public DirectDebitResponse requestDirectDebitAccount(String memberId, DirectDebitRequest request) {

        final UUID requestId = uuidGenerator.generateRandom();

        gateway.sendAndWait(new CreateTrustlySelectAccountOrderCommand(memberId, requestId));

        return startTrustlyOrder(memberId, request, requestId);

    }

    public DirectDebitResponse startTrustlyOrder(String memberId, DirectDebitRequest request, UUID requestId) {
        try {
            final Request trustlyRequest = createRequest(requestId, memberId, request);
            final Response response = api.sendRequest(trustlyRequest);

            if (response.successfulResult()) {
                final Map<String, Object> data;
                data = response.getResult().getData();
                log.info("SelectAccount Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}", data.get("orderid"), requestId);

                gateway.sendAndWait(
                        new SelectAccountResponseReceviedCommand(requestId, (String) data.get("url"), (String) data.get("orderid")));

                return new DirectDebitResponse((String) data.get("url"));
            } else {
                final Error error = response.getError();
                log.info("Order creation failed: {}, {}, {}", error.getName(), error.getCode(), error.getMessage());
                throw new RuntimeException("Got error from trustly");
            }
        }
        catch (TrustlyAPIException ex) {
            //gateway.sendAndWait(new SelectAccountRequestFailedCommand(requestId, ex.getMessage()));
            throw new RuntimeException("Failed calling trustly.", ex);
        }
    }

    private Request createRequest(UUID hedvigOrderId, String memberId, DirectDebitRequest request) {
        final SelectAccount.Build build = new SelectAccount.Build(NOTIFICATION_URL, memberId, hedvigOrderId.toString());
        build.requestDirectDebitMandate("1");
        build.firstName(request.getFirstName());
        build.lastName(request.getLastName());
        build.country(COUNTRY);
        build.email(request.getEmail());
        build.locale("sv_SE");
        build.nationalIdentificationNumber(request.getSsn());
        build.successURL(successUrl);
        build.failURL(failUrl);

        final Request request1 = build.getRequest();
        request1.getParams().getData().getAttributes().put("HoldNotifications", "1");

        return request1;
    }

    public Response sendRequest(Request request) {
        return api.sendRequest(request);
    }

    public ResponseStatus recieveNotification(Notification notification) {

        return ResponseStatus.OK;

    }
}
